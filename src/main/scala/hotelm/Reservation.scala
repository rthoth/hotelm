package hotelm

import java.time.LocalDateTime

final case class Reservation(roomNumber: String, client: String, checkIn: LocalDateTime, checkOut: LocalDateTime)
