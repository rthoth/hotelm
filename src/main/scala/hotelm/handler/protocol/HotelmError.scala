package hotelm.handler.protocol

import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

final case class HotelmError(message: String)

object HotelmError:

  given JsonEncoder[HotelmError] = DeriveJsonEncoder.gen
