package hotelm.module

import com.softwaremill.macwire.Module
import hotelm.repository.ReservationRepository
import hotelm.repository.RoomRepository
import zio.Task
import zio.ZIO

@Module
trait RepositoryModule:

  def roomRepository: RoomRepository

  def reservationRepository: ReservationRepository

object RepositoryModule:

  def apply(): Task[RepositoryModule] = ZIO.attempt(new Default())

  private class Default extends RepositoryModule:

    override val roomRepository: RoomRepository = RoomRepository(???, ???)

    override val reservationRepository: ReservationRepository = ReservationRepository(???, ???)
