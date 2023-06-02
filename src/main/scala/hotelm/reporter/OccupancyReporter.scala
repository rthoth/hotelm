package hotelm.reporter

import hotelm.OccupancyReport
import java.time.LocalDate
import zio.Task

trait OccupancyReporter:

  def report(date: LocalDate): Task[OccupancyReport]
