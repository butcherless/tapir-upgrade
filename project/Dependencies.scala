import sbt._

object Dependencies {

  val mainAndTest = Seq(

"com.softwaremill.sttp.tapir" %% "tapir-core"                 % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server"     % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"           % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"         % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml"   % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http" % Versions.tapir,


    "ch.qos.logback" % "logback-classic" % Versions.logback,
    "com.github.mlangc" %% "slf4zio" % Versions.slf4zio,
    "dev.zio" %% "zio" % Versions.zio,
    
    // TESTING
    
    "org.scalatest" %% "scalatest" % Versions.scalatest % Test,
        "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttp  % Test,
    "com.typesafe.akka" %% "akka-testkit"      % Versions.akka      % Test

  )
}
