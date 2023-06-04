package hotelm.manager

import com.softwaremill.macwire.wire
import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.repository.ReservationRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import zio.Task
import zio.ZIO

trait ReservationManager:

  def accept(reservation: Reservation, room: Room): Task[(Reservation, Room)]

  def search(date: LocalDate): Task[List[Reservation]]

object ReservationManager:

  def apply(repository: ReservationRepository, config: Config): ReservationManager = wire[Default]

  final case class Config(cleaningTime: Duration, minimumDuration: Duration, maximumDuration: Duration)

  private class Default(repository: ReservationRepository, config: Config) extends ReservationManager:

    override def accept(reservation: Reservation, room: Room) =
      for
        _        <- validate(reservation)
        previous <- repository.searchPrevious(room.number, reservation.checkIn)
        next     <- repository.searchNext(room.number, reservation.checkOut)
        _        <- checkAvailability(reservation, previous, next)
                      .flatMap(reportUnavailability(room))
        _        <- repository
                      .searchIntersection(room.number, reservation.checkIn, reservation.checkOut)
                      .filterOrFail(_.isEmpty)(HotelmException.RoomUnavailable(room.number))
        _        <- repository.add(reservation)
        _        <- ZIO.logInfo(s"A new reservation for room ${reservation.roomNumber} has been made.")
      yield (reservation, room)

    override def search(date: LocalDate): Task[List[Reservation]] =
      repository.search(date)

    private def validate(reservation: Reservation): Task[Unit] =
      val duration = Duration.between(reservation.checkIn, reservation.checkOut)
      if duration.compareTo(config.minimumDuration) < 0 then
        ZIO.fail(HotelmException.InvalidReservation("The reservation is too short!"))
      else if duration.compareTo(config.maximumDuration) > 0 then
        ZIO.fail(HotelmException.InvalidReservation("The reservation is too long!"))
      else ZIO.unit

    /** If it's no available, it'll return suggestions. */
    private def checkAvailability(
        reservation: Reservation,
        previous: Option[Reservation],
        next: Option[Reservation]
    ): Task[(Option[LocalDateTime], Option[LocalDateTime])] = ZIO.attempt {
      val previousSuggestion = previous match
        case Some(Reservation(_, _, _, _, checkOut)) =>
          val diff = Duration.between(checkOut, reservation.checkIn)
          if diff.compareTo(config.cleaningTime) > 0 then None
          else Some(checkOut.plus(config.cleaningTime))

        case None => None

      val nextSuggestion = next match
        case Some(Reservation(_, _, _, checkIn, _)) =>
          val diff = Duration.between(reservation.checkOut, checkIn)
          if diff.compareTo(config.cleaningTime) > 0 then None
          else Some(checkIn.minus(config.cleaningTime))

        case None => None

      (previousSuggestion, nextSuggestion)
    }

    private def reportUnavailability(room: Room)(
        suggestion: (Option[LocalDateTime], Option[LocalDateTime])
    ): Task[Unit] =
      suggestion match
        case (a, b) if a.isDefined || b.isDefined => ZIO.fail(HotelmException.RoomUnavailable(room.number))
        case _                                    => ZIO.unit
