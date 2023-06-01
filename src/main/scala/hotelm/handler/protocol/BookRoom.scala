package hotelm.handler.protocol

import java.time.LocalDateTime
import zio.json.DeriveJsonDecoder
import zio.json.JsonDecoder

case class BookRoom(client: String, checkIn: LocalDateTime, checkOut: LocalDateTime)

object BookRoom:
  given JsonDecoder[BookRoom] = DeriveJsonDecoder.gen
