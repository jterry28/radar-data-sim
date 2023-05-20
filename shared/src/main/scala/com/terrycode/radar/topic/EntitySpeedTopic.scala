package com.terrycode.radar.topic

import org.apache.kafka.clients.admin.NewTopic

import scala.jdk.CollectionConverters.*

object EntitySpeedTopic {
  val name           : String                    = "radar.entity.speed"
  val topicProperties: NewTopic                  = new NewTopic(name, 16, 1.toShort)
    .configs(
      Map (
        "compression.type" -> "lz4",
        "retention.bytes" -> "4294967296", // 4GB
        "retention.ms" -> "60000", // Low constraint for testing in local environment
        "segment.ms" -> "30000"
  ).asJava)

}
