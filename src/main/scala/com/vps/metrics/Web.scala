package com.vps.metrics

import java.io.FileInputStream

import com.twitter.finagle.Httpx
import io.finch.response.Ok
import io.finch.route._
import org.json4s._
import org.json4s.jackson.Serialization

import scala.io.Source

object Web {

  implicit val formats = Serialization.formats(NoTypeHints)

  val is = new FileInputStream("src/main/resources/index.html")
  val template = Source.fromInputStream(is).mkString

  def start = {
    Httpx.serve(":8080",
     get("devices") {
       Ok.withContentType(Option("application/json")) {
         Serialization.write(Map(
           "devices" -> Store.devices,
           "metrics" -> Store.mostRecentMetrics
         ))
       }
    } :+: get("index") {
       Ok.withContentType(Option("text/html"))(template)
    } toService)

  }

}
