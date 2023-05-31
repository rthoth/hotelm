package hotelm.handler.protocol

import hotelm.Room
import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

case class RoomCreated(room: Room)

object RoomCreated:

  given JsonEncoder[Room]        = DeriveJsonEncoder.gen
  given JsonEncoder[RoomCreated] = DeriveJsonEncoder.gen
