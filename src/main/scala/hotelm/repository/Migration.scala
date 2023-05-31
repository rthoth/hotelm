package hotelm.repository

import javax.sql.DataSource
import org.flywaydb.core.Flyway
import zio.Task
import zio.ZIO

object Migration:

  def apply(dataSource: DataSource): Task[DataSource] =
    ZIO.attemptBlocking {
      Flyway
        .configure()
        .dataSource(dataSource)
        .load()
        .migrate()
      dataSource
    }
