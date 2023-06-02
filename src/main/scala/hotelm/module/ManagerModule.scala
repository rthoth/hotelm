package hotelm.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wireWith
import hotelm.manager.ReservationManager
import hotelm.manager.RoomManager
import java.time.Duration
import zio.Task
import zio.ZIO

@Module
trait ManagerModule:

  def roomManager: RoomManager

  def reservationManager: ReservationManager

object ManagerModule:

  def apply(repositoryModule: RepositoryModule): Task[ManagerModule] = ZIO.attempt {
    new Default(repositoryModule)
  }

  private class Default(repositoryModule: RepositoryModule) extends ManagerModule:

    private val reservationManagerConfig = ReservationManager.Config(
      cleaningTime = Duration.ofHours(4),
      minimumDuration = Duration.ofHours(12),
      maximumDuration = Duration.ofDays(30)
    )

    override val reservationManager: ReservationManager = wireWith(ReservationManager.apply)

    override val roomManager: RoomManager = wireWith(RoomManager.apply)
