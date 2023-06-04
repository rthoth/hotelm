package hotelm.handler.protocol

import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

final case class HotelmError(message: String, `type`: Option[String] = None)

object HotelmError:

  def apply(throwable: Throwable): HotelmError =
    new HotelmError(throwable.getMessage, Some(throwable.getClass.getSimpleName))
  given JsonEncoder[HotelmError]               = DeriveJsonEncoder.gen
