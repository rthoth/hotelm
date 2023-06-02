package hotelm

import java.time.LocalDate
import java.time.LocalTime

case class OccupancyReport(
    date: LocalDate,
    rooms: Seq[RoomOccupancyReport]
)

case class RoomOccupancyReport(
    number: String,
    reservations: Seq[RoomOccupancyReservationReport]
)

case class RoomOccupancyReservationReport(
    checkIn: LocalTime,
    checkOut: LocalTime
)
