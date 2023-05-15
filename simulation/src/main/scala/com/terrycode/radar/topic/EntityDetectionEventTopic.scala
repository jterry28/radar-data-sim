package com.terrycode.radar.topic

import org.apache.kafka.clients.admin.NewTopic

import scala.jdk.CollectionConverters.*

object EntityDetectionEventTopic {
  val name           : String                    = "radar.entity.detection"
  val topicProperties: NewTopic                  = new NewTopic(name, 10, 1.toShort)
    .configs(
      Map (
        "compression.type" -> "gzip",
        "retention.bytes" -> "104857600", // 100MB
        "retention.ms" -> "60000", // Low constraint for testing in local environment
        "segment.ms" -> "30000"
  ).asJava)

}
