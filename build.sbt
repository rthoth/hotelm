ThisBuild / organization := "com.github.rthoth"
ThisBuild / version      := "0.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-Wunused:all"
)

val ZioVersion = "2.0.13"

lazy val root = (project in file("."))
  .settings(
    name := "hotelm",
    libraryDependencies ++= Seq(
      "dev.zio"                  %% "zio"               % ZioVersion,
      "dev.zio"                  %% "zio-http"          % "3.0.0-RC2",
      "io.getquill"              %% "quill-jdbc-zio"    % "4.6.0.1",
      "dev.zio"                  %% "zio-logging"       % "2.1.13",
      "dev.zio"                  %% "zio-logging-slf4j" % "2.1.13",
      "org.apache.logging.log4j"  % "log4j-slf4j-impl"  % "2.20.0",
      "com.softwaremill.macwire" %% "macros"            % "2.5.8"      % Provided,
      "com.softwaremill.macwire" %% "util"              % "2.5.8"      % Provided,
      "dev.zio"                  %% "zio-json"          % "0.5.0",
      "org.flywaydb"              % "flyway-core"       % "9.19.1",
      "com.h2database"            % "h2"                % "2.1.214",
      "io.github.arainko"        %% "ducktape"          % "0.1.8",
      "dev.zio"                  %% "zio-test"          % ZioVersion   % Test,
      "dev.zio"                  %% "zio-test-sbt"      % ZioVersion   % Test,
      "dev.zio"                  %% "zio-test-magnolia" % ZioVersion   % Test,
      "dev.zio"                  %% "zio-mock"          % "1.0.0-RC10" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
