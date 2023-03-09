ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "cqrs-projection",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.8",
      "org.typelevel" %% "cats-core" % "2.9.0",
      "co.fs2" %% "fs2-core" % "3.6.1",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1"
    )
  )
