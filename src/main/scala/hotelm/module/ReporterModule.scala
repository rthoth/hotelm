package hotelm.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wireWith
import hotelm.reporter.OccupancyReporter
import zio.Task
import zio.ZIO

@Module
trait ReporterModule:

  def occupancyReporter: OccupancyReporter

object ReporterModule:

  def apply(managerModule: ManagerModule): Task[ReporterModule] = ZIO.attempt {
    new Default(managerModule)
  }

  private class Default(managerModule: ManagerModule) extends ReporterModule:

    override def occupancyReporter: OccupancyReporter = wireWith(OccupancyReporter.apply)
