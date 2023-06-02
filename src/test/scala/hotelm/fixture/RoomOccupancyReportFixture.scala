package hotelm.fixture

import hotelm.RoomOccupancyReport
import scala.util.Random

object RoomOccupancyReportFixture:

  def createNew(): RoomOccupancyReport = RoomOccupancyReport(
    number = s"${1 + Random.nextInt(9)}${Random.any("A", "B", "C")}",
    reservations =
      if Random.nextBoolean() then
        for (_ <- 0 until 1 + Random.nextInt(3)) yield RoomBookingOccupancyReportFixture.createNew()
      else Nil
  )
