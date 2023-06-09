package hotelm

import zio.Runtime
import zio.ZLayer
import zio.logging.backend.SLF4J
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.testEnvironment

abstract class Spec extends ZIOSpecDefault:
  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j >>> testEnvironment
