package hotelm.module

import hotelm.handler.RoomHandler
import hotelm.handler.VacancyHandler
import zio.Task
import zio.ZIO

trait HandlerModule:

  def roomHandler: RoomHandler
  def vacancyHandler: VacancyHandler

object HandlerModule:

  def apply(managerModule: ManagerModule): Task[HandlerModule] = ZIO.attempt {
    new Default(managerModule)
  }

  private class Default(managerModule: ManagerModule) extends HandlerModule:

    override val roomHandler: RoomHandler = RoomHandler(managerModule.roomManager)

    override val vacancyHandler: VacancyHandler = VacancyHandler()
