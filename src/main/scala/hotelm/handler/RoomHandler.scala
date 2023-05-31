package hotelm.handler

import hotelm.HotelmException
import hotelm.Room
import hotelm.handler.protocol.CreateRoom
import hotelm.handler.protocol.RoomCreated
import hotelm.manager.RoomManager
import zio.IO
import zio.http.Request
import zio.http.Response
import zio.http.Status

trait RoomHandler:

  def add(req: Request): IO[Response, Response]

  def book(req: Request, number: String): IO[Response, Response]

  def remove(req: Request, number: String): IO[Response, Response]

object RoomHandler:

  def apply(manager: RoomManager): RoomHandler = new Default(manager)

  private class Default(manager: RoomManager) extends RoomHandler:

    override def add(req: Request): IO[Response, Response] =
      for
        command  <- req.asJson[CreateRoom]
        inserted <- manager
                      .add(Room(command.number, command.beds))
                      .mapError(handleError)
      yield responseAsJson(Status.Ok, RoomCreated(inserted))

    override def book(req: Request, number: String): IO[Response, Response] = ???

    override def remove(req: Request, number: String): IO[Response, Response] = ???
