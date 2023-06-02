package hotelm.reporter

import com.softwaremill.macwire.wire
import hotelm.OccupancyReport
import hotelm.Reservation
import hotelm.Room
import hotelm.RoomOccupancyReport
import hotelm.RoomOccupancyReservationReport
import hotelm.manager.ReservationManager
import hotelm.manager.RoomManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import zio.Task

trait OccupancyReporter:

  def report(date: LocalDate): Task[OccupancyReport]

object OccupancyReporter:

  def apply(roomManager: RoomManager, reservationManager: ReservationManager): OccupancyReporter =
    wire[Default]

  private class Default(roomManager: RoomManager, reservationManager: ReservationManager) extends OccupancyReporter:
    override def report(date: LocalDate): Task[OccupancyReport] =
      val start = LocalDateTime.of(date, LocalTime.of(0, 0))
      val end   = LocalDateTime.of(date, LocalTime.of(23, 59, 59))
      for
        rooms        <- roomManager.all
        reservations <- reservationManager.searchAll(date)
      yield OccupancyReport(
        date = date,
        rooms = rooms.map(reportFor(start, end, reservations.groupBy(_.roomNumber)))
      )

    private def reportFor(start: LocalDateTime, end: LocalDateTime, reservationByRoom: Map[String, List[Reservation]])(
        room: Room
    ): RoomOccupancyReport =
      RoomOccupancyReport(
        number = room.number,
        reservations = reservationByRoom.get(room.number) match
          case None               => Nil
          case Some(reservations) => reservations.map(reportFor(start, end))
      )

    private def reportFor(start: LocalDateTime, end: LocalDateTime)(
        reservation: Reservation
    ): RoomOccupancyReservationReport =
      val checkIn  = (if reservation.checkIn.compareTo(start) >= 0 then reservation.checkIn else start).toLocalTime
      val checkOut = (if reservation.checkOut.compareTo(end) <= 0 then reservation.checkOut else end).toLocalTime
      RoomOccupancyReservationReport(checkIn, checkOut)
