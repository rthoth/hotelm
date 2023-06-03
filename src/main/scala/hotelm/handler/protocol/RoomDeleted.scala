package hotelm.handler.protocol

import hotelm.Room
import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

case class RoomDeleted(number: String, beds: Int)

object RoomDeleted:

  given JsonEncoder[RoomDeleted] = DeriveJsonEncoder.gen

  def apply(room: Room): RoomDeleted = new RoomDeleted(room.number, room.beds)
