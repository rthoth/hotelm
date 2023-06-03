package hotelm.fixture

import hotelm.Room
import scala.util.Random

object RoomFixture:

  def createNew() =
    Room(number = s"${(100 + Random.nextInt(700))}${Random.any("A", "B", "C", "R", "Z")}", beds = 1 + Random.nextInt(3))
