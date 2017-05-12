name := "LoggingService"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in assembly := Some("com.LoggerItem.Controls")

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases",
"Bintray sbt plugin releases" at "http://dl.bintray.com/sbt/sbt-plugin-releases")



libraryDependencies ++= Seq(


  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.play" % "play-json_2.11" % "2.6.0-M6",
  "com.rabbitmq" % "amqp-client" % "4.0.2",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.5.0",
  "org.elasticsearch" % "elasticsearch" % "5.3.0",
  "org.elasticsearch.client" % "transport" % "5.3.0",
  "io.searchbox" % "jest" % "2.0.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
  "org.apache.logging.log4j" % "log4j-api" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.6.2"



)



assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.discard
    }
  case _ => MergeStrategy.first
}
