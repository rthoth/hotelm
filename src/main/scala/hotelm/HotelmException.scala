package hotelm

object HotelmException:

  abstract class InvalidUserInput(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class InvalidRoom(message: String, cause: Throwable = null) extends InvalidUserInput(message, cause)

  class InvalidReservation(message: String, cause: Throwable = null) extends InvalidUserInput(message, cause)

  class RoomUnavailable(message: String, cause: Throwable = null) extends InvalidUserInput(message, cause)

  class UnableToInsertRoom(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class UnableToInsertReservation(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class RoomNotFound(message: String, cause: Throwable = null) extends InvalidUserInput(message, cause)
