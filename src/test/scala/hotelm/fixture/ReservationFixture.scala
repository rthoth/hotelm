package hotelm.fixture

import hotelm.Reservation
import hotelm.Room
import java.util.UUID
import scala.util.Random

object ReservationFixture:

  def createNewWithRoom(room: Room = RoomFixture.createNew()) =
    createNew(room) -> room

  def createNew(room: Room = RoomFixture.createNew()) =
    val checkIn = LocalDateTimeFixture.createNew()
    Reservation(
      id = UUID.randomUUID().toString,
      roomNumber = room.number,
      client = Random.any("Galileo", "Marie Curie"),
      checkIn = checkIn,
      checkOut = checkIn.plusDays(3)
    )
