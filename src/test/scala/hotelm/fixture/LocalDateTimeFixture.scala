package hotelm.fixture

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import scala.util.Random

object LocalDateTimeFixture:

  def createNew() =
    LocalDateTime.now().plusDays(-10 + Random.nextLong(20)).truncatedTo(ChronoUnit.SECONDS)
