package hotelm.module

import hotelm.Reservation
import hotelm.handler.RoomHandler
import hotelm.handler.VacancyHandler
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.http.*

object HttpAppModule:

  def apply(handlerModule: HandlerModule): Task[Nothing] =
    for
      app    <- createApp(handlerModule)
      result <- Server.serve(app).provide(Server.default)
    yield result

  private def createApp(handlerModule: HandlerModule): Task[App[Any]] = ZIO.succeed {
    val reservationIdGeneratorLayer = ZLayer.succeed(Reservation.IdGenerator)

    Http.collectZIO[Request] {
      case req @ Method.POST -> Root / "room"                      => handlerModule.roomHandler.add(req)
      case req @ Method.DELETE -> Root / "room" / number           => handlerModule.roomHandler.remove(req, number)
      case req @ Method.POST -> Root / "room" / number / "booking" => handlerModule.roomHandler.book(req, number).provideLayer(reservationIdGeneratorLayer)
      case req @ Method.GET -> Root / "vacancy" / date             => handlerModule.vacancyHandler.search(req, date)
    }
  }
