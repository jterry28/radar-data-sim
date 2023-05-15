package com.terrycode.radar.topic

import org.apache.kafka.clients.admin.NewTopic

import scala.jdk.CollectionConverters.*

object EntityDetectionEventTopic {
  val name           : String                    = "radar.entity.detection"
  val topicProperties: NewTopic                  = new NewTopic(name, 10, 0.toShort)
    .configs(
      Map {
        "compression.type" -> "gzip"
        "retention.bytes" -> "1073741824" // 1GB
        "retention.ms" -> "30000" // Low constraint for testing in local environment
      }.asJava)

}
