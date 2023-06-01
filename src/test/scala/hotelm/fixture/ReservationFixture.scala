package hotelm.fixture

import hotelm.Reservation

object ReservationFixture:

  def createNew() =
    val room    = RoomFixture.createNew()
    val checkIn = LocalDateTimeFixture.createNew()
    Reservation(roomNumer = room.number, client = "Einstein", checkIn = checkIn, checkOut = checkIn.plusDays(3)) -> room
