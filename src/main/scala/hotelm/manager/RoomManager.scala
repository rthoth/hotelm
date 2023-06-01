package hotelm.manager

import com.softwaremill.macwire.wire
import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.repository.RoomRepository
import zio.Cause
import zio.Task
import zio.ZIO

trait RoomManager:

  def add(room: Room): Task[Room]

  def remove(number: String): Task[Room]

  def accept(reservation: Reservation): Task[(Reservation, Room)]

object RoomManager:

  def apply(repository: RoomRepository, reservationManager: ReservationManager): RoomManager =
    wire[Default]

  private class Default(roomRepository: RoomRepository, reservationManager: ReservationManager) extends RoomManager:

    override def add(room: Room): Task[Room] =
      for
        validated    <- validate(room)
                          .tapErrorCause(ZIO.logWarningCause("The new room is invalid!", _))
        insertedRoom <- roomRepository
                          .add(validated)
                          .tap(_ => ZIO.logInfo(s"A new room ${validated.number} with ${validated.beds} was added."))
                          .tapErrorCause(ZIO.logWarningCause("It was impossible to add a new room!", _))
      yield insertedRoom

    private def validate(room: Room): Task[Room] =
      if room.beds <= 0 then ZIO.fail(HotelmException.InvalidRoom("Number os beds is invalid!"))
      else if room.number.isBlank then ZIO.fail(HotelmException.InvalidRoom("The room's number is invalid!"))
      else ZIO.succeed(room)

    override def accept(reservation: Reservation): Task[(Reservation, Room)] =
      for
        room <- roomRepository
                  .get(reservation.roomNumer)
                  .someOrFail(HotelmException.RoomNotFound(reservation.roomNumer))
                  .tapErrorCause(ZIO.logWarningCause(s"It was impossible to find room ${reservation.roomNumer}!", _))
        _    <- reservationManager
                  .accept(reservation, room)
                  .tapErrorCause(
                    ZIO.logWarningCause(s"It was impossible to make a reservation for room ${reservation.roomNumer}!", _)
                  )
      yield reservation -> room

    override def remove(number: String): Task[Room] =
      for removed <- roomRepository
                       .remove(number)
                       .someOrFail(HotelmException.RoomNotFound(number))
      yield removed
