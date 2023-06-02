package hotelm.handler.protocol

import hotelm.OccupancyReport
import hotelm.RoomOccupancyReport
import hotelm.RoomOccupancyReservationReport
import io.github.arainko.ducktape.Transformer
import io.github.arainko.ducktape.to
import java.time.LocalDate
import java.time.LocalTime
import zio.json.DeriveJsonEncoder
import zio.json.JsonEncoder

final case class OccupancyReportResponse(
    date: LocalDate,
    rooms: Seq[RoomOccupancyReportResponse]
)

object OccupancyReportResponse:

  given JsonEncoder[OccupancyReportResponse] = DeriveJsonEncoder.gen

  def apply(report: OccupancyReport): OccupancyReportResponse =
    report.to[OccupancyReportResponse]

final case class RoomOccupancyReportResponse(
    number: String,
    reservations: Seq[RoomOccupancyReservationReportResponse]
)

object RoomOccupancyReportResponse:

  given JsonEncoder[RoomOccupancyReportResponse] = DeriveJsonEncoder.gen

  given Transformer[RoomOccupancyReport, RoomOccupancyReportResponse] =
    Transformer.define.build()

final case class RoomOccupancyReservationReportResponse(
    checkIn: LocalTime,
    checkOut: LocalTime
)

object RoomOccupancyReservationReportResponse:

  given JsonEncoder[RoomOccupancyReservationReportResponse] = DeriveJsonEncoder.gen

  given Transformer[RoomOccupancyReservationReport, RoomOccupancyReservationReportResponse] =
    Transformer.define.build()
