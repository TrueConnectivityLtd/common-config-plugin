# common-config-plugin

## Usage 

Under any sbt module, in project/plugins.sbt, add the following resolver and plugin : 

```scala

resolvers +="build-releases" at "http://build.cakesolutions.net/artifactory/plugins-release-local"

addSbtPlugin("com.unshackled" % "common-config-plugin" % "0.2.0")

```

Then, write an build.sbt file in the following fashion : 

```scala

name := "module-name"

version := "0.0.1" 

unshackledCommonSettings
```

This will pull common useful dependencies, set the usual scalac options, set the scala version to 2.11.7, set the organization to "com.unshackled", add several useful plugins to the project, such as a formatter and a style checker (the plugins have been pre-configured, but their configuration can be overridden in the build.sbt file). Then, in sbt, call 

```
scalastyleGenerateConfig 
```

which will generate the scalastyle-config.xml file required by scalastyle. After that, use sbt as usual. 

Have fun, good luck ! 
