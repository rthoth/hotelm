package hotelm

object HotelmException:

  abstract class InvalidUserInput(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  class InvalidRoom(message: String, cause: Throwable = null) extends InvalidUserInput(message, cause)

  class UnableToInsertRoom(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
