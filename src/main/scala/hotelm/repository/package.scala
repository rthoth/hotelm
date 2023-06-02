package hotelm.repository

import io.getquill.*
import io.github.arainko.ducktape.Transformer
import java.sql.Timestamp
import java.time.LocalDateTime
import zio.Task
import zio.ZIO

given Transformer[LocalDateTime, Timestamp] = Timestamp.valueOf(_)

given Transformer[Timestamp, LocalDateTime] = _.toLocalDateTime()

def convertTo[T]: PartiallyAppliedConversion[T] = new PartiallyAppliedConversion[T]

class PartiallyAppliedConversion[T]:

  def apply[I](value: I)(using transformer: Transformer[I, T]): Task[T] =
    ZIO.attempt(transformer.transform(value))

// https://github.com/zio/zio-protoquill/issues/208#issuecomment-1378695160
extension (inline timestamp: Timestamp)
  inline def <=(other: Timestamp) = quote(sql"$timestamp <= $other".as[Boolean])
  inline def >=(other: Timestamp) = quote(sql"$timestamp >= $other".as[Boolean])
