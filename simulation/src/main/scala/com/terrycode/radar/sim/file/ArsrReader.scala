package com.terrycode.radar.sim.file

import cats.effect.{IO, Resource}
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.terrycode.radar.sim.file.ArsrGeoJson

import java.nio.file.{Files, Path}
import scala.util.Try

object ArsrReader {

  def readFromFile(path: Path): IO[ArsrGeoJson] = {
    Resource.fromAutoCloseable {
      IO {
        Files.newInputStream(path)
      }
    }.use(s => IO(readFromStream[ArsrGeoJson](s)))
  }

}
