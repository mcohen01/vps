package com.vps.metrics

import java.util.Date

import com.datastax.driver.core.{Cluster, Session}
import com.twitter.storehaus.cache.{MapCache, Memoize}
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime

object Store {

  val conf: Config  = ConfigFactory.load();
  val host = conf.getString("casssandra.host")
  val keyspace = conf.getString("casssandra.keyspace")

  private def connection(host: String) = Cluster.builder()
                                                .addContactPoint(host)
                                                .build()
                                                .connect()

  private val cache = MapCache.empty[String, Session].toMutable()

  private val session = Memoize(cache)(connection)

  val fields = "device_id, event_time, temperature, current, voltage"
  private def cql(mostRecent: Boolean) = {
    val table = if (mostRecent) "metric" else "most_recent_metric"
    s"""insert into vps.$table ($fields) values (?,?,?,?,?);"""
  }

  /**
   * We do two writes here, one into the analysis table, the other into the rollup,
   * to support showing last known metric in the UI. The only difference is the
   * primary key for the rollup table does not include the timestamp, only the device id,
   * so there will only ever be one "row" per device.
   */
  def writeMetric(deviceId: String, metric: Map[String, Float]) = {
    write(cql(false), deviceId, metric)
    write(cql(true), deviceId, metric)
  }

  private def write(query: String, deviceId: String, metric: Map[String, Float]) = {
    // we could cache the prepared statement, but i'm not bothering with that here...
    val ps = session(host).prepare(query)
    session(host).execute(ps.bind(deviceId.asInstanceOf[Object],
                                   DateTime.now.toDate.asInstanceOf[Object],
                                   metric.get("temperature").get.asInstanceOf[Object],
                                   metric.get("current").get.asInstanceOf[Object],
                                   metric.get("voltage").get.asInstanceOf[Object]))
  }

  def markConnection(deviceId: String, connected: Boolean) = {
    val ps = session(host).prepare("insert into vps.device (device_id, disconnected) values (?,?);")
    val disconnected = if (connected) null else DateTime.now.toDate.asInstanceOf[Object]
    session(host).execute(ps.bind(deviceId.asInstanceOf[Object], disconnected))
  }


  case class Metric(
    device_id: String,
    event_time: Date,
    temperature: Float,
    current: Float,
    voltage: Float
  )

  import scala.collection.JavaConverters._

  def devices = {
    val cql = "select * from vps.device;"
    session(host).execute(cql).all().asScala map { r =>
      Metric(r.getString("device_id"), r.getDate("disconnected"), 0f, 0f, 0f)
    } toList
  }

  def mostRecentMetrics = {
    val cql = "select * from vps.most_recent_metric;"
    session(host).execute(cql).all().asScala map { r =>
      Metric(r.getString("device_id"),
             r.getDate("event_time"),
             r.getFloat("temperature"),
             r.getFloat("current"),
             r.getFloat("voltage"))
    } toList
  }

}
