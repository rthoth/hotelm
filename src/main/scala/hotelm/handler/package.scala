package hotelm.handler

import hotelm.HotelmException
import hotelm.handler.protocol.HotelmError
import zio.IO
import zio.ZIO
import zio.http.Body
import zio.http.Request
import zio.http.Response
import zio.http.Status
import zio.json.*
import zio.stream.ZStream

extension (req: Request) def asJson[T: JsonDecoder]: IO[Response, T] = ???

def responseAsJson[T: JsonEncoder](status: Status, payload: T): Response =
  Response(status, body = Body.fromString(payload.toJson))

def handleError(cause: Throwable): Response =
  cause match
    case _: HotelmException.InvalidUserInput => responseAsJson(Status.BadRequest, HotelmError(cause.getMessage))
    case _                                   => responseAsJson(Status.InternalServerError, HotelmError("Unexpected internal error!"))
