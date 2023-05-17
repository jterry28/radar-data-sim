package com.terrycode.radar.sim

import cats.effect.*
import com.terrycode.radar.sim.model.VirtualClock
import com.terrycode.radar.sim.stream.{FlyersTree, ScanStream}
import com.terrycode.radar.topic.EntityDetectionEventTopic
import fs2.*

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val kafkaBootstrapServer = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

    val clock = VirtualClock(10)

    val flyersTree   = FlyersTree[IO](clock, 200000)
    val searchStream = ScanStream(flyersTree.treeRef, clock, kafkaBootstrapServer)

    flyersTree.makeStream
      .concurrently(searchStream.makeStream)
      .compile.drain.as(ExitCode.Success)
  }
}