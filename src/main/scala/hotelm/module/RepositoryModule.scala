package hotelm.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wireWith
import hotelm.repository.ReservationRepository
import hotelm.repository.RoomRepository
import io.getquill.H2ZioJdbcContext
import io.getquill.SnakeCase
import java.io.File
import org.h2.jdbcx.JdbcDataSource
import zio.Task
import zio.ZIO
import zio.ZLayer

@Module
trait RepositoryModule:

  def roomRepository: RoomRepository

  def reservationRepository: ReservationRepository

object RepositoryModule:

  def apply(file: File): Task[RepositoryModule] = ZIO.attempt(new Default(file))

  private class Default(file: File) extends RepositoryModule:

    private val dataSourceLayer = ZLayer.fromZIO {
      ZIO.attemptBlocking {
        val dataSource = JdbcDataSource()
        dataSource.setUrl(s"jdbc:h2:file:${file.getCanonicalPath}")
        dataSource.setUser("sa")
        dataSource.setPassword("sa")
        dataSource
      }
    }

    private val context = new H2ZioJdbcContext(SnakeCase)

    override val roomRepository: RoomRepository = wireWith(RoomRepository.apply)

    override val reservationRepository: ReservationRepository = wireWith(ReservationRepository.apply)
