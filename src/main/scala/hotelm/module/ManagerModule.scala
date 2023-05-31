package hotelm.module

import hotelm.manager.RoomManager
import zio.Task
import zio.ZIO

trait ManagerModule:

  def roomManager: RoomManager

object ManagerModule:

  def apply(repositoryModule: RepositoryModule): Task[ManagerModule] = ZIO.attempt {
    new Default(repositoryModule)
  }

  private class Default(repositoryModule: RepositoryModule) extends ManagerModule:

    override def roomManager: RoomManager = RoomManager(repositoryModule.roomRepository)
