package hotelm.fixture

import hotelm.RoomOccupancyReservationReport

object RoomBookingOccupancyReportFixture:

  def createNew(): RoomOccupancyReservationReport =
    val checkIn = LocalDateTimeFixture.createNew().toLocalTime
    RoomOccupancyReservationReport(
      checkIn = checkIn,
      checkOut = checkIn.plusHours(2)
    )
