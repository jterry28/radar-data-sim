package com.terrycode.radar.sim

import cats.effect.*
import cats.effect.std.Queue
import cats.{*, given}
import com.github.plokhotnyuk.rtree2d.core.*
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth.*
import com.terrycode.radar.sim.file.ArsrGeoJson.*
import com.terrycode.radar.sim.file.ArsrReader
import com.terrycode.radar.sim.model.{BoundingBox, FlyingObject, Radar}
import com.terrycode.radar.topic.EntityDetectionEvent.given
import com.terrycode.radar.topic.{EntityDetectionEvent, EntityDetectionEventTopic, JsoniterCodecToKafka}
import fs2.*
import fs2.io.file.*
import fs2.kafka.*

import java.nio.file
import java.time.{Instant, ZoneId, ZoneOffset}
import scala.concurrent.duration.*

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val kafkaBootstrapServer = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

    KafkaAdminClient.resource[IO](AdminClientSettings.apply(kafkaBootstrapServer))
      .use(c => c.createTopic(EntityDetectionEventTopic.topicProperties))

    val producer = KafkaProducer.pipe {
      ProducerSettings(Serializer[IO, String], JsoniterCodecToKafka.serializer[EntityDetectionEvent])
        .withBootstrapServers(kafkaBootstrapServer)
        .withBatchSize(64000) // Send only once batch size or linger limit is reached
        .withLinger(20.millis)
    }

    val startTime = Instant.now()
    val flyers    = (0 to 1000000)
      .map(_ => FlyingObject.withinBounds(BoundingBox.US_LOWER_48, startTime))
      .map(f => entry(f.initialLat, f.initialLon, f))
      .toList

    val tree = RTree(flyers)

    val queue = Queue.unbounded[IO, Radar]

    Stream.eval(queue).flatMap { q =>
      val fill = Stream
        .evalSeq {
          ArsrReader.readFromFile(Path("./simulation/src/main/resources/arsr4geo.json").toNioPath)
            .map(toRadar)
        }
        .parEvalMapUnorderedUnbounded { r =>
          IO.sleep(r.initialDelay).flatMap(_ => q.offer(r))
        }

      val requeuePipe: Pipe[IO, Radar, Unit] = s => {
        s.parEvalMapUnorderedUnbounded(r => IO.sleep(r.scanDelay).flatMap(_ => q.offer(r)))
      }

      val producePipe: Pipe[IO, Radar, ProducerResult[String, EntityDetectionEvent]] = s => {
        s.parEvalMapUnorderedUnbounded { r =>
          val now      = Instant.now()
          val zonedNow = now.atZone(ZoneOffset.UTC)
          val scan     = r.nextScanArea()
          val result   = tree.searchAll(scan.minLon, scan.minLat, scan.maxLon, scan.maxLat)
            .map { e =>
              val (lat, lon) = e.value.getCurrentPosition(now)
              ProducerRecord(EntityDetectionEventTopic.name,
                             r.name,
                             EntityDetectionEvent(r.name, zonedNow, e.value.name, lat, lon))
            }
          IO(ProducerRecords(result.toList))
        }.through(producer)
      }

      val consume = Stream.fromQueueUnterminated(q)
        .broadcastThrough(requeuePipe,
                          producePipe)

      consume.concurrently(fill)
    }.compile.drain.as(ExitCode.Success)

  }
}