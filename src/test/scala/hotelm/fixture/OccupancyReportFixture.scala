package hotelm.fixture

import hotelm.OccupancyReport
import java.time.LocalDate
import scala.util.Random

object OccupancyReportFixture:

  def createNew(date: LocalDate = LocalDateTimeFixture.createNew().toLocalDate): OccupancyReport = OccupancyReport(
    date = date,
    rooms = for _ <- 0 until 1 + Random.nextInt(3) yield RoomOccupancyReportFixture.createNew()
  )
