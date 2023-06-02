package hotelm.fixture

import hotelm.Reservation
import java.util.UUID

object ReservationFixture:

  def createNew() =
    val room    = RoomFixture.createNew()
    val checkIn = LocalDateTimeFixture.createNew()
    Reservation(
      id = UUID.randomUUID().toString,
      roomNumber = room.number,
      client = "Einstein",
      checkIn = checkIn,
      checkOut = checkIn.plusDays(3)
    ) -> room
