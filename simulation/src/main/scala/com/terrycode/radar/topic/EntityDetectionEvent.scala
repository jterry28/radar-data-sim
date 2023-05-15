package com.terrycode.radar.topic

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import java.time.ZonedDateTime

final case class EntityDetectionEvent(radarId: String, capturedTime: ZonedDateTime, entityName: String, lat: Float, lon: Float)

object EntityDetectionEvent {
  given codec: JsonValueCodec[EntityDetectionEvent] = JsonCodecMaker.make[EntityDetectionEvent]
}
