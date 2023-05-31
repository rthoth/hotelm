package hotelm.repository

import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import zio.RLayer
import zio.Scope
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer

object H2DataSource:

  def layer: RLayer[Scope, DataSource] = {
    val databaseName = s"database-${System.currentTimeMillis()}"

    ZLayer.fromZIO {
      ZIO.acquireRelease(
        for
          dataSource <- createDataSource(databaseName)
          _          <- Migration(dataSource)
        yield dataSource
      )(dataSource =>
        ZIO.attemptBlocking {
          val connection = dataSource.getConnection()
          connection.createStatement().execute("DROP ALL OBJECTS")
          connection.close()
        }.orDie
      )
    }
  }

  def createDataSource(name: String): Task[DataSource] = ZIO.attemptBlocking {
    val dataSource = new JdbcDataSource()
    dataSource.setURL(s"jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1")
    dataSource.setUser("sa")
    dataSource.setPassword("sa")
    dataSource
  }
