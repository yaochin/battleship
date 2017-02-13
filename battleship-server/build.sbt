name := "battleship-server"


val finatra = "2.8.0"

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % finatra,

  "com.twitter" %% "finatra-http" % finatra % "test",
  "com.twitter" %% "inject-server" % finatra % "test",
  "com.twitter" %% "finatra-jackson" % finatra % "test",
  "com.twitter" %% "inject-app" % finatra % "test",
  "com.twitter" %% "inject-core" % finatra % "test",
  "com.twitter" %% "inject-modules" % finatra % "test",

  "com.google.inject.extensions" % "guice-testlib" % "4.0" % "test",

  "com.twitter" %% "finatra-http" % finatra % "test" classifier "tests",
  "com.twitter" %% "finatra-jackson" % finatra % "test" classifier "tests",
  "com.twitter" %% "inject-server" % finatra % "test" classifier "tests",
  "com.twitter" %% "inject-app" % finatra % "test" classifier "tests",
  "com.twitter" %% "inject-core" % finatra % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % finatra % "test" classifier "tests",

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