package hotelm.handler.protocol

import hotelm.Room
import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

case class RoomCreated(number: String, beds: Int)

object RoomCreated:

  def apply(room: Room) = new RoomCreated(number = room.number, beds = room.beds)

  given JsonEncoder[RoomCreated] = DeriveJsonEncoder.gen
