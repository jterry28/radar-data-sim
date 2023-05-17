package com.terrycode.radar.sim.model

import com.terrycode.radar.sim.model.BoundingBox
import com.terrycode.radar.sim.model.FlyingObject.*

import java.time.{Duration, Instant}
import java.util.UUID
import scala.concurrent.duration.*
import scala.util.Random

case class FlyingObject(name           : String,
                        initializedTime: Instant,
                        initialLat     : Float,
                        initialLon     : Float,
                        altitude       : Float,
                        knots          : Float,
                        courseDeg      : Float) {

  def currentPosition(currentTime: Instant, timeDilation: Long): (Float, Float) = {
    val timeElapsed = FiniteDuration(Duration.between(initializedTime, currentTime).getSeconds, SECONDS) * timeDilation
    currentPosition(timeElapsed)
  }

  def currentPosition(elapsedDuration: FiniteDuration): (Float, Float) = {
    val bearing       = Math.toRadians(courseDeg)
    val hoursTraveled = elapsedDuration.toSeconds / 3600f
    val distanceRatio = knots * hoursTraveled * kmPerNM / radius
    // Haversine Formula
    val lat           = Math.toRadians(initialLat)
    val lon           = Math.toRadians(initialLon)
    val distRatioCos  = Math.cos(distanceRatio)
    val distRatioSin  = Math.sin(distanceRatio)
    val sinLat        = Math.sin(lat)
    val cosLat        = Math.cos(lat)

    val lat2 = Math.toDegrees(Math.asin((sinLat * distRatioCos) + (cosLat * distRatioSin * Math.cos(bearing))))
    val lon2 = Math.toDegrees(lon + Math.atan2(Math.sin(bearing) * distRatioSin * cosLat,
                                               distRatioCos - sinLat * Math.sin(lat2)))
    (normalizeLat(lat2).toFloat, normalizeLon(lon2).toFloat)
  }

  private def normalizeLon(lon: Double): Double = {
    (lon % 360 + 540) % 360 - 180
  }

  private def normalizeLat(lat: Double): Double = {
    (lat % 180 + 270) % 180 - 90
  }

}

object FlyingObject {
  private val radius  = 6378.1370 // Radius of the Earth kilometers
  private val kmPerNM = 1.852

  def withinBounds(box: BoundingBox, initializedTime: Instant): FlyingObject = {
    FlyingObject(UUID.randomUUID().toString,
                 initializedTime,
                 Random.between(box.minLat, box.maxLat),
                 Random.between(box.minLon, box.maxLon),
                 Random.between(500, 160000), // Typical altitudes of various flying objects
                 Random.between(25, 2000), // Typical speeds
                 Random.between(0, 360)) // Possible course in degrees
  }
}
