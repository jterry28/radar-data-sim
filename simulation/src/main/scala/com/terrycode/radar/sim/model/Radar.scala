package com.terrycode.radar.sim.model

import com.terrycode.radar.sim.model.Radar.*
import com.github.plokhotnyuk.rtree2d.core.{RTree, RTreeEntry}

import scala.concurrent.duration.*
import scala.util.Random

final class Radar(val name            : String,
                  val lat             : Float,
                  val lon             : Float,
                  val rangeNMiles     : Float,
                  private val scanRate: FiniteDuration) {

  private val scanChunks = {
    val rangeDegrees = nauticalMilesToDegrees(rangeNMiles)
    val maxLat       = lat + rangeDegrees
    val minLat       = lat - rangeDegrees
    val maxLon       = lon + rangeDegrees
    val minLon       = lon - rangeDegrees

    Array[ScanArea](ScanArea(lon, lat, maxLon, maxLat),
                    ScanArea(lon, minLat, maxLon, lat),
                    ScanArea(minLon, minLat, lon, lat),
                    ScanArea(minLon, lat, lon, maxLat))
  }

  lazy val initialDelay: FiniteDuration = FiniteDuration(Random.between(0, scanRate.toMillis), MILLISECONDS)

  private var currentScanChunk = 0

  def nextScanArea(): ScanArea = {
    this.synchronized {
      val current = currentScanChunk
      currentScanChunk = (currentScanChunk + 1) % scanChunks.length
      scanChunks(current)
    }
  }

  def scanDelay: FiniteDuration = (scanRate / scanChunks.length) + FiniteDuration(Random.between(-10, 10), MILLISECONDS) // jitter

}

object Radar {
  private def nauticalMilesToDegrees(miles: Float) = miles / 60f // Not accurate for longitude

  case class ScanArea(minLon: Float, minLat: Float, maxLon: Float, maxLat: Float)

  extension[A] (tree: RTree[A]) {
    def searchAll(area: ScanArea): Seq[RTreeEntry[A]] = {
      tree.searchAll(area.minLon, area.minLat, area.maxLon, area.maxLat)
    }
  }
}
