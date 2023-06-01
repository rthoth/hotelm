package hotelm.handler.protocol

import hotelm.Reservation
import java.time.LocalDateTime
import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

case class BookRoomResponse(number: String, client: String, checkIn: LocalDateTime, checkOut: LocalDateTime)

object BookRoomResponse:

  def apply(reservation: Reservation) =
    new BookRoomResponse(reservation.roomNumer, reservation.client, reservation.checkIn, reservation.checkOut)

  given JsonEncoder[BookRoomResponse] = DeriveJsonEncoder.gen
