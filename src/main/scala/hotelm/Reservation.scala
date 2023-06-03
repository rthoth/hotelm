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

  given Ordering[Reservation] = (a, b) =>
    val x = a.checkIn.compareTo(b.checkIn)
    if x != 0 then x else a.checkOut.compareTo(b.checkOut)

  trait IdGenerator:
    def nextId: String

  object IdGenerator extends IdGenerator:
    override def nextId: String = UUID.randomUUID().toString
