package hotelm.manager

import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.repository.RoomRepository
import zio.Task
import zio.ZIO

trait RoomManager:

  def add(room: Room): Task[Room]

  def remove(number: String): Task[Room]

  def accept(reservation: Reservation): Task[(Reservation, Room)]

object RoomManager:

  def apply(repository: RoomRepository): RoomManager = new Default(repository)

  private class Default(repository: RoomRepository) extends RoomManager:

    override def add(room: Room): Task[Room] =
      for
        validated    <- validate(room)
                          .tapErrorCause(ZIO.logWarningCause("The new room is invalid!", _))
        insertedRoom <- repository
                          .add(validated)
                          .tap(_ => ZIO.logInfo(s"A new room ${validated.number} with ${validated.beds} was added."))
                          .tapErrorCause(ZIO.logWarningCause("It was impossible to add a new room!", _))
      yield insertedRoom

    private def validate(room: Room): Task[Room] =
      if room.beds <= 0 then ZIO.fail(HotelmException.InvalidRoom("Number os beds is invalid!"))
      else if room.number.isBlank then ZIO.fail(HotelmException.InvalidRoom("The room's number is invalid!"))
      else ZIO.succeed(room)

    override def accept(reservation: Reservation): Task[(Reservation, Room)] =
      for room <- repository
                    .get(reservation.roomNumer)
                    .someOrFail(HotelmException.RoomNotFound(reservation.roomNumer))
        
      yield ???

    override def remove(number: String): Task[Room] =
      for removed <- repository
                       .remove(number)
                       .someOrFail(HotelmException.RoomNotFound(number))
      yield removed
