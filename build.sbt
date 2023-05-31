ThisBuild / organization := "com.github.rthoth"
ThisBuild / version      := "0.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0"

val ZioVersion = "2.0.13"

lazy val root = (project in file("."))
  .settings(
    name := "hotelm",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"               % ZioVersion,
      "dev.zio"       %% "zio-http"          % "3.0.0-RC2",
      "io.getquill"   %% "quill-jdbc-zio"    % "4.6.0.1",
      "dev.zio"       %% "zio-json"          % "0.5.0",
      "org.flywaydb"   % "flyway-core"       % "9.19.1",
      "com.h2database" % "h2"                % "2.1.214",
      "dev.zio"       %% "zio-test"          % ZioVersion % Test,
      "dev.zio"       %% "zio-test-sbt"      % ZioVersion % Test,
      "dev.zio"       %% "zio-test-magnolia" % ZioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
