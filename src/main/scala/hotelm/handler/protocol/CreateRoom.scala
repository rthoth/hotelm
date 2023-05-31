package hotelm.handler.protocol

import zio.json.DeriveJsonDecoder
import zio.json.JsonDecoder

final case class CreateRoom(number: String, beds: Int)

object CreateRoom:
  given JsonDecoder[CreateRoom] = DeriveJsonDecoder.gen
