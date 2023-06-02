package hotelm

import java.time.LocalDateTime
import java.util.UUID

final case class Reservation(
    id: String,
    roomNumber: String,
    client: String,
    checkIn: LocalDateTime,
    checkOut: LocalDateTime
)

object Reservation:

  trait IdGenerator:
    def nextId: String

  object IdGenerator extends IdGenerator:
    override def nextId: String = UUID.randomUUID().toString
