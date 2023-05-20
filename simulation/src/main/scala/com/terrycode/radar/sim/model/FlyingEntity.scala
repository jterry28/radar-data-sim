package com.terrycode.radar.sim.model

import com.terrycode.radar.math.HaversineFormulae
import com.terrycode.radar.sim.model.BoundingBox
import com.terrycode.radar.sim.model.FlyingEntity.*

import java.time.{Duration, Instant}
import java.util.UUID
import scala.concurrent.duration.*
import scala.util.Random
import scala.jdk.DurationConverters.*

case class FlyingEntity(name           : String,
                        initializedTime: Instant,
                        initialLat     : Float,
                        initialLon     : Float,
                        altitude       : Float,
                        knots          : Float,
                        courseDeg      : Float) {

  def currentPosition(elapsedDuration: FiniteDuration): (Float, Float) =
    HaversineFormulae.calculateDestination(initialLat, initialLon, courseDeg, knots, elapsedDuration)

}

object FlyingEntity {
  def withinBounds(box: BoundingBox, initializedTime: Instant): FlyingEntity = {
    FlyingEntity(UUID.randomUUID().toString,
                 initializedTime,
                 Random.between(box.minLat, box.maxLat),
                 Random.between(box.minLon, box.maxLon),
                 Random.between(500, 160000), // Typical altitudes of various flying objects
                 Random.between(25, 2000), // Typical speeds
                 Random.between(0, 360)) // Possible course in degrees
  }
}
