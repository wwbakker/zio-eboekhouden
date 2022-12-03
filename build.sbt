ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val dispatchVersion = "1.1.3"
lazy val dispatch = "org.dispatchhttp" %% "dispatch-core" % dispatchVersion
lazy val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1"
lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
lazy val zio = Seq(
    "dev.zio" %% "zio" % "2.0.4",
    "dev.zio" %% "zio-config" % "3.0.2",
    "dev.zio" %% "zio-config-magnolia" % "3.0.2",
    "dev.zio" %% "zio-config-typesafe" % "3.0.2"
)

lazy val root = (project in file("."))
  .enablePlugins(ScalaxbPlugin)
  .settings(
    name := "nl.wwbakker.eboekhouden",
    Compile / scalaxb / scalaxbPackageName := "eboekhouden",
    Compile / scalaxb / scalaxbDispatchVersion := dispatchVersion,
    libraryDependencies ++= Seq(dispatch, jaxbApi, scalaParser, scalaXml) ++ zio
  )
