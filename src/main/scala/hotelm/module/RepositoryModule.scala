package hotelm.module

import hotelm.repository.RoomRepository
import zio.Task
import zio.ZIO

trait RepositoryModule:

  def roomRepository: RoomRepository

object RepositoryModule:

  def apply(): Task[RepositoryModule] = ZIO.attempt(new Default())

  private class Default extends RepositoryModule:

    override def roomRepository: RoomRepository = RoomRepository(???, ???)
