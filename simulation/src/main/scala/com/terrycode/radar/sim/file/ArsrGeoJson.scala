package com.terrycode.radar.sim.file

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.terrycode.radar.sim.model.*

case class ArsrGeoJson(`type`: String, features: List[Site])
case class Site(`type`: String, properties: Properties, geometry: Geometry)
case class Properties(name: String, desc: String)
case class Geometry(`type`: String, coordinates: List[Float])

object ArsrGeoJson {
  import scala.concurrent.duration.*
  def toRadar(json: ArsrGeoJson): List[Radar] = {
    json.features.map { site =>
      Radar(site.properties.name, site.geometry.coordinates.head, site.geometry.coordinates.last, 250f, 10.seconds)
    }
  }
  
  given codec: JsonValueCodec[ArsrGeoJson] = JsonCodecMaker.make[ArsrGeoJson]
}