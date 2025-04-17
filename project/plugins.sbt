addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.2")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.2.2")

// For signing artifacts (PGP)
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1") // Or the latest version

// For publishing to Sonatype (Maven Central)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21") // Or the latest version
