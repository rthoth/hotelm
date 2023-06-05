package hotelm

object HotelmException:

  class InvalidParameter(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class InvalidRoom(message: String, cause: Throwable = null) extends InvalidParameter(message, cause)

  class InvalidReservation(message: String, cause: Throwable = null) extends InvalidParameter(message, cause)

  class RoomUnavailable(message: String, cause: Throwable = null) extends InvalidParameter(message, cause)

  class UnableToInsertRoom(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class UnableToInsertReservation(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class RoomNotFound(message: String, cause: Throwable = null) extends InvalidParameter(message, cause)
