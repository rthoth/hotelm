package hotelm.fixture

import hotelm.Room
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Random

object RoomFixture:

  private val number = AtomicInteger(100)

  def createNew() =
    Room(
      number =
        s"${(number.getAndIncrement() + Random.nextInt(700))}${Random.any("A", "B", "C", "D", "E", "F", "R", "Z")}",
      beds = 1 + Random.nextInt(3)
    )
