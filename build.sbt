name := "vps"

version := "1.0"

scalaVersion := "2.11.7"

mainClass in Compile := Some("com.vps.metrics.Broker")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"

libraryDependencies += "org.eclipse.paho" % "mqtt-client" % "0.4.0"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.7"

libraryDependencies += "com.twitter" %% "storehaus-core" % "0.12.0"

libraryDependencies += "com.twitter" %% "storehaus-cache" % "0.12.0"

libraryDependencies += "com.github.finagle" %% "finch-core" % "0.8.0"

libraryDependencies += "com.github.finagle" %% "finch-json4s" % "0.8.0"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.2.0"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.10"

resolvers += "MQTT Repository" at "https://repo.eclipse.org/content/repositories/paho-releases/"