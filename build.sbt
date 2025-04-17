lazy val root = project
  .in(file("."))
  .settings(
    name := "Rate Limiter",
    version := "1.0.0",
    scalaVersion := "3.6.2",
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

    // Credentials for Sonatype (not stored in build.sbt for security, but used by plugin)
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "s01.oss.sonatype.org", //or oss.sonatype.org
      sys.env.getOrElse("SONATYPE_USERNAME", "YOUR_SONATYPE_USERNAME"),
      sys.env.getOrElse("SONATYPE_PASSWORD", "YOUR_SONATYPE_PASSWORD")
    ),

    // PGP settings (for signing)
    // pgpSecretKey := sys.env.get("PGP_SECRET_KEY"),
    // pgpPassphrase := Some(sys.env("PGP_PASSPHRASE").toCharArray),

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


    // This is crucial for cross-publishing
    crossScalaVersions := Seq("3.6.2"), // Add all supported Scala versions

    // Release process
    // releaseCrossBuild := true, //cross build
  )
