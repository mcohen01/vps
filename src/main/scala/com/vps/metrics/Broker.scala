package com.vps.metrics

import com.typesafe.config.{Config, ConfigFactory}
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.util.Try

object Broker {

  var client: Option[MqttClient] = None
  val conf: Config  = ConfigFactory.load();

  def main(args: Array[String]) {

    val brokerUrl    = conf.getString("broker.url")
    val metricsTopic = conf.getString("broker.topics.metrics")
    val metaTopic    = conf.getString("broker.topics.meta")

    client = Some(new MqttClient(brokerUrl,
                                 MqttClient.generateClientId,
                                 new MemoryPersistence))

    client.get.connect()

    client.get.subscribe(metricsTopic)
    client.get.subscribe(metaTopic)

    client.get.setCallback(new MqttCallback {
      override def messageArrived(topic: String, message: MqttMessage): Unit =
        Try {
          topic match {
            case x if x == metricsTopic => writeMetric(message)
            case x if x == metaTopic => writeMeta(message)
          }
        } map identity recover {
          case x => x.printStackTrace() // TODO something more robust...
        }


      override def connectionLost(cause: Throwable): Unit = {
        println(cause)
        // do something more robust (i.e. nagios, pingdom, etc...)
      }

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}
    })

    Web.start

  }

  def shutdown() = client foreach (_.disconnect())

  def writeMetric(message: MqttMessage) = {
    var deviceId: String = null
    val msg = new String(message.getPayload)
    val metric = msg.split(";").foldLeft(Map.empty[String, Float]) { (m, s) =>
      val pair = s.split("=")
      // this is really ugly, updating some mutable state inside a fold,
      // but i'm not going to let purity get in the way of a quick and dirty solution
      if (pair.head == "device_id") {
        deviceId = pair.last
        m
      } else m + (pair.head -> pair.last.toFloat)
    }
    Store.writeMetric(deviceId, metric)
  }

  def writeMeta(message: MqttMessage) = {
    val msg = new String(message.getPayload)
    val Array(deviceId, connected) = msg.split(";")
    Store.markConnection(deviceId.split("=")(1), connected == "connect")
  }

}
