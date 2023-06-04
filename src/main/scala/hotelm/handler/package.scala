package hotelm.handler

import hotelm.HotelmException
import hotelm.handler.protocol.HotelmError
import java.nio.charset.StandardCharsets
import zio.IO
import zio.ZIO
import zio.http.Body
import zio.http.Header
import zio.http.Headers
import zio.http.MediaType
import zio.http.Request
import zio.http.Response
import zio.http.Status
import zio.json.*

extension (req: Request)
  def asJson[T: JsonDecoder]: IO[Response, T] =
    req.header(Header.ContentType) match
      case Some(value) if value.mediaType == MediaType.application.json =>
        extractJson(req.body)
      case _                                                            =>
        ZIO.fail(responseAsJson(Status.BadRequest, HotelmError("It was expected an application/json!")))

def extractJson[T: JsonDecoder](body: Body): IO[Response, T] =
  for
    json    <- body
                 .asString(StandardCharsets.UTF_8)
                 .mapError(handleError)
    decoded <- ZIO
                 .fromEither(json.fromJson)
                 .tapErrorCause(ZIO.logWarningCause("It was impossible to parse the json!", _))
                 .mapError(x => responseAsJson(Status.BadRequest, HotelmError(x)))
  yield decoded

def responseAsJson[T: JsonEncoder](status: Status, payload: T): Response =
  Response(status, body = Body.fromString(payload.toJson))

def handleError(cause: Throwable): Response =
  cause match
    case _: HotelmException.InvalidUserInput => responseAsJson(Status.BadRequest, HotelmError(cause))
    case _                                   => responseAsJson(Status.InternalServerError, HotelmError("Unexpected internal error!"))
