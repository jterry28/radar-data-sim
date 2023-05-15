package com.terrycode.radar.topic

import cats.effect.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import fs2.kafka.*

import java.nio.charset.StandardCharsets
import scala.util.Try

object JsoniterCodecToKafka {

  def serializer[T](implicit codec: JsonValueCodec[T]): Serializer[IO, T] =
    Serializer.lift(t => IO(writeToString(t).getBytes(StandardCharsets.UTF_8)))

  def deserializer[T](implicit codec: JsonValueCodec[T]): Deserializer[IO, T] =
    Deserializer.lift(data => IO(readFromString[T](new String(data, StandardCharsets.UTF_8))))

}
