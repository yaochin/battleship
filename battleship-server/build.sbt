name := "battleship-server"

libraryDependencies ++= Seq(
  "com.twitter" % "finatra-http_2.11" % "2.2.0",
  "com.github.xiaodongw" %% "swagger-finatra" % "0.7.1",
  "io.swagger" % "swagger-core" % "1.5.12",

  "com.google.inject" % "guice" % "4.0" withSources(),
  "com.google.guava" % "guava" % "18.0" withSources(),

  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.5.0" withSources(),
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.0" withSources(),
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.5.0" withSources(),

  "org.mockito" % "mockito-all" % "1.8.5" % "test" withSources(),
  "org.scalatest" % "scalatest_2.11" % "3.0.0" withSources(),

  "joda-time" % "joda-time" % "2.9.4" withSources(),

  "ch.qos.logback" % "logback-classic" % "1.1.7"

)