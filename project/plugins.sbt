ivyLoggingLevel := UpdateLogging.Quiet
scalacOptions in Compile ++= Seq("-feature", "-deprecation")

// Code formatter
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.2.1")

// Style, Formatting and Linting
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// Release
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.12")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
