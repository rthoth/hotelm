package hotelm.handler.protocol

import hotelm.Room
import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

given JsonEncoder[Room] = DeriveJsonEncoder.gen
