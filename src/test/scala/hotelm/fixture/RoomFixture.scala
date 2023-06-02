package hotelm.fixture

import hotelm.Room
import scala.util.Random

object RoomFixture:

  def createNew() =
    Room(number = s"${(100 + Random.nextInt(500))}${Random.any("A", "B", "C", "Z")}", beds = 1 + Random.nextInt(3))
