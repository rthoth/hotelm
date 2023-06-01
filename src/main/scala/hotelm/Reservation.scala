package hotelm

import java.time.LocalDateTime

final case class Reservation(roomNumer: String, client: String, checkIn: LocalDateTime, checkOut: LocalDateTime)
