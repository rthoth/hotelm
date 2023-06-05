package hotelm.repository

import hotelm.HotelmException
import org.h2.api.ErrorCode
import org.h2.jdbc.JdbcException

trait ExceptionMapper:

  def apply(message: => String)(cause: Throwable): Throwable

object H2ExceptionMapper extends ExceptionMapper:

  override def apply(message: => String)(cause: Throwable): Throwable =
    cause match
      case h2: JdbcException if h2.getErrorCode == ErrorCode.DUPLICATE_KEY_1 =>
        HotelmException.InvalidParameter(message, cause)

      case _ => cause
