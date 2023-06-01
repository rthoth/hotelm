package hotelm.fixture

import hotelm.Room
import scala.util.Random

object RoomFixture:

  def createNew() = Room(number = s"${(10 + Random.nextInt(40))}A", beds = 1 + Random.nextInt(3))
