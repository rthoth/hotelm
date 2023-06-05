package hotelm.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wireWith
import hotelm.repository.Migration
import hotelm.repository.ReservationRepository
import hotelm.repository.RoomRepository
import io.getquill.H2ZioJdbcContext
import io.getquill.SnakeCase
import java.io.File
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import zio.Task
import zio.ZIO
import zio.ZLayer

@Module
trait RepositoryModule:

  def roomRepository: RoomRepository

  def reservationRepository: ReservationRepository

object RepositoryModule:

  def apply(file: File): Task[RepositoryModule] =
    for dataSource <- createDataSource(file) yield Default(dataSource)

  private def createDataSource(file: File): Task[DataSource] =
    for
      dataSource <- ZIO.attempt {
                      val dataSource = JdbcDataSource()
                      dataSource.setUrl(s"jdbc:h2:file:${file.getCanonicalPath}")
                      dataSource.setUser("sa")
                      dataSource.setPassword("sa")
                      dataSource
                    }
      _          <- Migration(dataSource)
    yield dataSource

  private class Default(dataSource: DataSource) extends RepositoryModule:

    private val exceptionMapper = hotelm.repository.H2ExceptionMapper

    private val context = new H2ZioJdbcContext(SnakeCase)

    private val dataSourceLayer = ZLayer.succeed(dataSource)

    override val roomRepository: RoomRepository = wireWith(RoomRepository.apply)

    override val reservationRepository: ReservationRepository = wireWith(ReservationRepository.apply)
