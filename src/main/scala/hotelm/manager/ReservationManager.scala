package hotelm.manager

import com.softwaremill.macwire.wire
import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.repository.ReservationRepository
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import zio.Task
import zio.ZIO
import zio.json.EncoderOps

trait ReservationManager:

  def accept(reservation: Reservation, room: Room): Task[(Reservation, Room)]

object ReservationManager:

  def apply(repository: ReservationRepository, config: Config): ReservationManager = wire[Default]

  final case class Config(cleaningTime: Duration, minimumDuration: Duration, maximumDuration: Duration)

  private class Default(repository: ReservationRepository, config: Config) extends ReservationManager:

    override def accept(reservation: Reservation, room: Room) =
      for
        _        <- validate(reservation)
        previous <- repository.searchPrevious(room.number, reservation.checkIn)
        _        <- checkAvailability(reservation, previous)
                      .flatMap(reportUnavailability(room))
        _        <- repository
                      .searchIntersection(room.number, reservation.checkIn, reservation.checkOut)
                      .filterOrFail(_.isEmpty)(HotelmException.RoomUnavailable(room.number))
        _        <- repository.add(reservation)
        _        <- ZIO.logInfo(s"A new reservation for room ${reservation.roomNumber} has been made.")
      yield (reservation, room)

    private def validate(reservation: Reservation): Task[Unit] =
      val duration = Duration.between(reservation.checkIn, reservation.checkOut)
      if duration.compareTo(config.minimumDuration) < 0 then
        ZIO.fail(HotelmException.InvalidReservation("The reservation is too short!"))
      else if duration.compareTo(config.maximumDuration) > 0 then
        ZIO.fail(HotelmException.InvalidReservation("The reservation is too long!"))
      else ZIO.unit

    /** If it's no available, it'll return a suggestion. */
    private def checkAvailability(
        reservation: Reservation,
        previous: Option[Reservation]
    ): Task[Option[LocalDateTime]] = ZIO.attempt {
      previous match
        case Some(Reservation(_, _, _, _, checkOut)) =>
          val diff = Duration.between(checkOut, reservation.checkIn)
          if diff.compareTo(config.cleaningTime) > 0 then None
          else Some(checkOut.plus(config.cleaningTime))

        case None => None
    }

    private def reportUnavailability(room: Room)(suggestion: Option[LocalDateTime]): Task[Unit] =
      suggestion match
        case Some(_) => ZIO.fail(HotelmException.RoomUnavailable(room.number))
        case None    => ZIO.unit
