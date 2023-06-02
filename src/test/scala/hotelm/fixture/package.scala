package hotelm.fixture

import scala.util.Random

extension (random: Random)
  def any[A](seq: A*): A =
    seq(random.nextInt(seq.size))
