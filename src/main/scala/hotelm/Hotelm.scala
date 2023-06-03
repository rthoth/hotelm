package hotelm

import hotelm.module.HandlerModule
import hotelm.module.HttpAppModule
import hotelm.module.ManagerModule
import hotelm.module.ReporterModule
import hotelm.module.RepositoryModule
import java.io.File
import zio.Scope
import zio.System
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object Hotelm extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      h2File           <- System
                            .env("HOTELM_H2_FILE")
                            .someOrFail("Please, define the HOTELM_H2_FILE environment!")
                            .map(File(_))
      repositoryModule <- RepositoryModule(h2File)
      managerModule    <- ManagerModule(repositoryModule)
      reporterModule   <- ReporterModule(managerModule)
      handlerModule    <- HandlerModule(managerModule, reporterModule)
      ret              <- HttpAppModule(handlerModule)
    yield ret
