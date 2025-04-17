lazy val root = project
  .in(file("."))
  .settings(
    name := "Rate Limiter",
    version := "1.0.1",
    scalaVersion := "3.6.2",
    idePackagePrefix := Some("org.rate.limiter"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "org.xerial" % "sqlite-jdbc" % "3.49.1.0",
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    ),
  )
