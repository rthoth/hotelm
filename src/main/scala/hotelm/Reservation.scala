package hotelm

import java.time.LocalDateTime
import java.util.UUID

final case class Reservation(
    id: String,
    roomNumber: String,
    client: String,
    checkIn: LocalDateTime,
    checkOut: LocalDateTime
)

object Reservation:

  def apply(roomNumber: String, client: String, checkIn: LocalDateTime, checkOut: LocalDateTime): Reservation =
    new Reservation(
      id = UUID.randomUUID().toString,
      roomNumber = roomNumber,
      client = client,
      checkIn = checkIn,
      checkOut = checkOut
    )
