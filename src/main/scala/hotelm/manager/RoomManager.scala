package hotelm.manager

import com.softwaremill.macwire.wire
import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.repository.RoomRepository
import zio.Task
import zio.ZIO

trait RoomManager:

  def add(room: Room): Task[Room]

  def all: Task[List[Room]]

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

    override def all: Task[List[Room]] =
      for result <- roomRepository.all yield result

    private def validate(room: Room): Task[Room] =
      if room.beds <= 0 then ZIO.fail(HotelmException.InvalidRoom("Number os beds is invalid!"))
      else if room.number.isBlank then ZIO.fail(HotelmException.InvalidRoom("The room's number is invalid!"))
      else ZIO.succeed(room)

    override def accept(reservation: Reservation): Task[(Reservation, Room)] =
      for
        room <- roomRepository
                  .get(reservation.roomNumber)
                  .tapErrorCause(ZIO.logWarningCause(s"It was impossible to find room ${reservation.roomNumber}!", _))
                  .someOrFail(HotelmException.RoomNotFound(reservation.roomNumber))
        _    <-
          reservationManager
            .accept(reservation, room)
            .tap(_ =>
              ZIO.logInfo(
                s"A new reservation was accepted for room ${room.number}, client=${reservation.client}, checkIn=${reservation.checkIn} and checkOut=${reservation.checkOut}."
              )
            )
            .tapErrorCause(
              ZIO.logWarningCause(s"It was impossible to make a reservation for room ${reservation.roomNumber}!", _)
            )
      yield reservation -> room

    override def remove(number: String): Task[Room] =
      for removed <- roomRepository
                       .remove(number)
                       .someOrFail(HotelmException.RoomNotFound(number))
                       .tap(_ => ZIO.logInfo(s"Room ${number} was been removed."))
                       .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove room ${number}!", _))
      yield removed
