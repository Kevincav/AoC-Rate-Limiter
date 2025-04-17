lazy val root = project
  .in(file("."))
  .settings(
    name := "Rate Limiter",
    version := "1.0.0",
    scalaVersion := "3.6.2",
    crossScalaVersions := Seq("2.13.12", "3.6.2"),
    idePackagePrefix := Some("org.rate.limiter"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "org.xerial" % "sqlite-jdbc" % "3.49.1.0",
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    ),

    //sonatype
    publishTo := sonatypePublishToBundle.value,
    sonatypeCredentialHost := "s01.oss.sonatype.org", //or oss.sonatype.org

    // Additional settings for Maven Central
    licenses := Seq("MIT License" -> url("https://opensource.org/license/mit")),
    homepage := Some(url("https://github.com/Kevincav/AoC-Rate-Limiter")), // Replace
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/Kevincav/AoC-Rate-Limiter"),
        "git",
        "https://github.com/Kevincav/AoC-Rate-Limiter"
      )
    ),

    developers := List(
      Developer(
        id    = "Kevincav",
        name  = "Kevin Melkowski",
        email = "kevin.melkowski@gmail.com",
        url   = url("https://github.com/Kevincav/AoC-Rate-Limiter")
      )
    ),
  )
