ivyLoggingLevel := UpdateLogging.Quiet
scalacOptions in Compile ++= Seq("-feature", "-deprecation")

// Code formatter
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// Style, Formatting and Linting
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// Release
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
