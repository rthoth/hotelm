package hotelm

import hotelm.module.HandlerModule
import hotelm.module.HttpAppModule
import hotelm.module.ManagerModule
import hotelm.module.ReporterModule
import hotelm.module.RepositoryModule
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object Hotelm extends ZIOAppDefault:

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    for
      repositoryModule <- RepositoryModule()
      managerModule    <- ManagerModule(repositoryModule)
      reporterModule   <- ReporterModule(repositoryModule)
      handlerModule    <- HandlerModule(managerModule, reporterModule)
      ret              <- HttpAppModule(handlerModule)
    yield ret
