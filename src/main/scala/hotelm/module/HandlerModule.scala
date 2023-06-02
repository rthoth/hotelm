package hotelm.module

import com.softwaremill.macwire.wireWith
import hotelm.handler.OccupancyHandler
import hotelm.handler.RoomHandler
import zio.Task
import zio.ZIO

trait HandlerModule:

  def roomHandler: RoomHandler
  def occupancyHandler: OccupancyHandler

object HandlerModule:

  def apply(managerModule: ManagerModule, reporterModule: ReporterModule): Task[HandlerModule] = ZIO.attempt {
    Default(managerModule, reporterModule)
  }

  private class Default(managerModule: ManagerModule, reporterModule: ReporterModule) extends HandlerModule:

    override val roomHandler: RoomHandler = wireWith(RoomHandler.apply)

    override val occupancyHandler: OccupancyHandler = wireWith(OccupancyHandler.apply)
