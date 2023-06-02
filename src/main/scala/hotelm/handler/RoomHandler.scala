package hotelm.handler

import hotelm.HotelmException
import hotelm.Reservation
import hotelm.Room
import hotelm.handler.protocol.BookRoom
import hotelm.handler.protocol.BookRoomResponse
import hotelm.handler.protocol.CreateRoom
import hotelm.handler.protocol.RoomCreated
import hotelm.manager.RoomManager
import zio.IO
import zio.ZIO
import zio.http.Request
import zio.http.Response
import zio.http.Status

trait RoomHandler:

  def add(req: Request): IO[Response, Response]

  def book(req: Request, number: String): ZIO[Reservation.IdGenerator, Response, Response]

  def remove(req: Request, number: String): IO[Response, Response]

object RoomHandler:

  def apply(manager: RoomManager): RoomHandler = new Default(manager)

  private class Default(manager: RoomManager) extends RoomHandler:

    override def add(req: Request): IO[Response, Response] =
      for
        command <- req
                     .asJson[CreateRoom]
        added   <- manager
                     .add(Room(command.number, command.beds))
                     .tapErrorCause(ZIO.logErrorCause("It was impossible to add a new room!", _))
                     .mapError(handleError)
      yield responseAsJson(Status.Ok, RoomCreated(added))

    override def book(req: Request, number: String): ZIO[Reservation.IdGenerator, Response, Response] =
      for
        command <- req.asJson[BookRoom]
        newId   <- ZIO.serviceWith[Reservation.IdGenerator](_.nextId)
        result  <- manager
                     .accept(Reservation(newId, number, command.client, command.checkIn, command.checkOut))
                     .mapError(handleError)
      yield responseAsJson(Status.Accepted, BookRoomResponse(result._1))

    override def remove(req: Request, number: String): IO[Response, Response] = ???
