package com.terrycode.radar.math

import com.terrycode.radar.math.TimeUtils.*

import scala.concurrent.duration.FiniteDuration
import scala.math.*

object HaversineFormulae {
  private val radius  = 6378.1370f // Radius of the Earth kilometers
  private val kmPerNM = 1.852f // Kilometers per nautical mile

  def calculateDestination(lat: Float, lon: Float, courseDeg: Float, knots: Float, elapsedDuration: FiniteDuration): (Float, Float) = {
    val hoursTraveled = elapsedDuration.toHoursFractional
    val distance      = knots * hoursTraveled * kmPerNM
    calculateDestination(lat, lon, courseDeg, distance)
  }

  def calculateDestination(lat: Float, lon: Float, courseDeg: Float, distance: Float): (Float, Float) = {
    val bearing       = toRadians(courseDeg)
    val distanceRatio = distance / radius
    // Haversine Formula
    val latRad        = toRadians(lat)
    val lonRad        = toRadians(lon)
    val distRatioCos  = cos(distanceRatio)
    val distRatioSin  = sin(distanceRatio)
    val sinLat        = sin(latRad)
    val cosLat        = cos(latRad)

    val lat2 = asin((sinLat * distRatioCos) + (cosLat * distRatioSin * cos(bearing)))
    val lon2 = lonRad + atan2(sin(bearing) * distRatioSin * cosLat,
                              distRatioCos - sinLat * sin(lat2))

    (normalizeLat(lat2.toDegrees).toFloat, normalizeLon(lon2.toDegrees).toFloat)
  }

  def calculateDistanceNauticalMiles(lat1: Float, lon1: Float, lat2: Float, lon2: Float): Float = {
    val dLat = (lat2 - lat1).toRadians
    val dLon = (lon2 - lon1).toRadians

    val a = pow(sin(dLat / 2), 2) + pow(sin(dLon / 2), 2) * cos(lat1.toRadians) * cos(lat2.toRadians)
    val c = 2 * asin(sqrt(a))
    (radius * c / kmPerNM).toFloat
  }

  private def normalizeLon(lon: Double): Double = {
    (lon % 360 + 540) % 360 - 180
  }

  private def normalizeLat(lat: Double): Double = {
    (lat % 180 + 270) % 180 - 90
  }

}
