package hotelm

import hotelm.module.HandlerModule
import hotelm.module.HttpAppModule
import hotelm.module.ManagerModule
import hotelm.module.ReporterModule
import hotelm.module.RepositoryModule
import java.io.File
import zio.Runtime
import zio.Scope
import zio.System
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault
import zio.ZLayer
import zio.logging.backend.SLF4J

object Hotelm extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      h2File           <- System
                            .env("HOTELM_H2_FILE")
                            .someOrFail("Please, define environment variable HOTEL_H2_FILE.")
                            .map(File(_))
      hostname         <- System
                            .env("HOTELM_HOSTNAME")
                            .someOrFail("Please, define environment variable HOTELM_HOSTNAME.")
      port             <- System
                            .env("HOTELM_PORT")
                            .someOrFail("Please, define environment variable HOTELM_PORT.")
                            .map(_.toInt)
      repositoryModule <- RepositoryModule(h2File)
      managerModule    <- ManagerModule(repositoryModule)
      reporterModule   <- ReporterModule(managerModule)
      handlerModule    <- HandlerModule(managerModule, reporterModule)
      server           <- HttpAppModule(hostname, port, handlerModule).fork
      _                <- ZIO.logInfo(s"\uD83C\uDFE8 Hotelm has started @ $hostname:$port.")
      exit             <- server.join
    yield exit
