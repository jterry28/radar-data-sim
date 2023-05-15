package com.terrycode.radar.sim.model

case class BoundingBox(minLon: Float, minLat: Float, maxLon: Float, maxLat: Float)

object BoundingBox {
  final val US_LOWER_48 = BoundingBox(-124.848974, 24.396308, -66.885444, 49.384358)
}