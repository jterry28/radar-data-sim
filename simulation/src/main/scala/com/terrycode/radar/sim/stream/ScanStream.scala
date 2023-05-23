package com.terrycode.radar.sim.stream

import cats.effect.*
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import com.github.plokhotnyuk.rtree2d.core.*
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth.*
import com.terrycode.radar.sim.file.*
import com.terrycode.radar.sim.model.*
import com.terrycode.radar.topic.*
import com.terrycode.radar.topic.JsoniterCodecToKafka.given
import fs2.*
import fs2.io.file.Path
import fs2.kafka.*
import org.apache.kafka.common.errors.TopicExistsException

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.duration.*

final class ScanStream(treeRef             : IO[Ref[IO, RTree[FlyingEntity]]],
                       clock               : VirtualClock[IO],
                       kafkaBootstrapServer: String) {
  KafkaAdminClient.resource[IO](AdminClientSettings(kafkaBootstrapServer))
    .use(c => c.createTopic(EntityDetectionEventTopic.topicProperties))
    .recover {
      case _: TopicExistsException => IO.unit
    }
    .unsafeRunSync()

  private val queue = Queue.unbounded[IO, Radar]

  def makeStream: Stream[IO, Any] = Stream.eval(queue)
    .flatMap { queue =>
      val fill = Stream
        .evalSeq {
          ArsrReader.readFromFile(Path("./simulation/src/main/resources/arsr4geo.json").toNioPath)
            .map(ArsrGeoJson.toRadar)
        }
        .parEvalMapUnorderedUnbounded { radar =>
          clock.scaledSleep(radar.initialDelay).flatMap(_ => queue.offer(radar))
        }

      val requeuePipe: Pipe[IO, Radar, Unit] = stream => {
        stream.parEvalMapUnorderedUnbounded(r => clock.scaledSleep(r.scanDelay).flatMap(_ => queue.offer(r)))
      }

      val producer = KafkaProducer.pipe {
        ProducerSettings(Serializer[IO, String], EntityDetectionEvent.codec)
          .withBootstrapServers(kafkaBootstrapServer)
          .withBatchSize(128000) // Send only once batch size or linger limit is reached
          .withLinger(20.millis)
      }

      val producePipe: Pipe[IO, Radar, ProducerResult[String, EntityDetectionEvent]] = stream => {
        Stream.eval(treeRef)
          .flatMap { ref =>
            stream.parEvalMapUnorderedUnbounded { radar =>
              val (now, duration) = clock.currentScaledTime
              val scan            = radar.nextScanArea()
              ref.get
                .map(_.searchAll(scan))
                .map { seq =>
                  seq.map { entry =>
                    val (lat, lon) = entry.value.currentPosition(duration)
                    ProducerRecord(EntityDetectionEventTopic.name,
                                   entry.value.name,
                                   EntityDetectionEvent(radar.name, LocalDateTime.ofInstant(now, ZoneOffset.UTC), entry.value.name, lat, lon))
                  }
                }.map(ProducerRecords(_))
            }.through(producer)
          }
      }

      val consume = Stream
        .fromQueueUnterminated(queue)
        .broadcastThrough(requeuePipe,
                          producePipe)

      consume.concurrently(fill)
    }
}
