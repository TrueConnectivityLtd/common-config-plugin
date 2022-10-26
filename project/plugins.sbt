ivyLoggingLevel := UpdateLogging.Quiet
scalacOptions in Compile ++= Seq("-feature", "-deprecation")

// Code formatter
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// Style, Formatting and Linting
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// Release
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
