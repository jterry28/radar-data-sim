package com.terrycode.radar.topic

import cats.effect.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import fs2.kafka.*

object JsoniterCodecToKafka {

  given codecToSerializer[T]: Conversion[JsonValueCodec[T], Serializer[IO, T]] =
    codec => Serializer.lift(t => IO(writeToArray(t)(codec)))

  given codecToDeserializer[T]: Conversion[JsonValueCodec[T], Deserializer[IO, T]] =
    codec => Deserializer.lift(d => IO(readFromArray(d)(codec)))

}
