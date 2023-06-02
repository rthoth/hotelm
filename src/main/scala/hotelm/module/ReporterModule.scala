package hotelm.module

import com.softwaremill.macwire.Module
import hotelm.reporter.OccupancyReporter
import zio.Task
import zio.ZIO

@Module
trait ReporterModule:

  def occupancyReporter: OccupancyReporter

object ReporterModule:

  def apply(repositoryModule: RepositoryModule): Task[ReporterModule] = ZIO.attempt {
    new Default(repositoryModule)
  }

  private class Default(repositoryModule: RepositoryModule) extends ReporterModule:

    override def occupancyReporter: OccupancyReporter = ???
