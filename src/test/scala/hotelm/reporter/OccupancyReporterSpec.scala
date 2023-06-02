package hotelm.reporter

import hotelm.OccupancyReport
import hotelm.RoomOccupancyReport
import hotelm.RoomOccupancyReservationReport
import hotelm.Spec
import hotelm.fixture.LocalDateTimeFixture
import hotelm.fixture.ReservationFixture
import hotelm.fixture.RoomFixture
import hotelm.manager.ReservationManager
import hotelm.manager.ReservationManagerMock
import hotelm.manager.RoomManager
import hotelm.manager.RoomManagerMock
import java.time.LocalDateTime
import java.time.LocalTime
import zio.ZIO
import zio.ZLayer
import zio.mock.Expectation
import zio.test.Assertion
import zio.test.assertTrue

object OccupancyReporterSpec extends Spec:

  private val reporterLayer = ZLayer.fromFunction(OccupancyReporter.apply)

  def spec = suite("OccupancyReporterSpec")(
    test("It should return a report for any day.") {

      val date  = LocalDateTimeFixture.createNew().toLocalDate
      val room1 = RoomFixture.createNew()
      val room2 = RoomFixture.createNew()
      val room3 = RoomFixture.createNew()

      val r11 = ReservationFixture
        .createNew(room1)
        .copy(
          checkIn = LocalDateTime.of(date.minusDays(1), LocalTime.of(12, 0)),
          checkOut = LocalDateTime.of(date, LocalTime.of(7, 0))
        )

      val r12 = ReservationFixture
        .createNew(room1)
        .copy(
          checkIn = LocalDateTime.of(date, LocalTime.of(13, 30)),
          checkOut = LocalDateTime.of(date.plusDays(2), LocalTime.of(7, 0))
        )

      val r31 = ReservationFixture
        .createNew(room3)
        .copy(
          checkIn = LocalDateTime.of(date.minusDays(2), LocalTime.of(14, 0)),
          checkOut = LocalDateTime.of(date.plusDays(3), LocalTime.of(7, 0))
        )

      val roomManager = RoomManagerMock.All(
        assertion = Assertion.anything,
        result = Expectation.value(List(room1, room2, room3))
      )

      val reservationManager = ReservationManagerMock.SearchAll(
        assertion = Assertion.equalTo(date),
        result = Expectation.value(List(r11, r12, r31))
      )

      (for report <- ZIO.serviceWithZIO[OccupancyReporter](_.report(date))
      yield assertTrue(
        report == OccupancyReport(
          date = date,
          rooms = Seq(
            RoomOccupancyReport(
              number = room1.number,
              reservations = Seq(
                RoomOccupancyReservationReport(
                  checkIn = LocalTime.of(0, 0),
                  checkOut = LocalTime.of(7, 0)
                ),
                RoomOccupancyReservationReport(
                  checkIn = LocalTime.of(13, 30),
                  checkOut = LocalTime.of(23, 59, 59)
                )
              )
            ),
            RoomOccupancyReport(
              number = room2.number,
              reservations = Nil
            ),
            RoomOccupancyReport(
              number = room3.number,
              reservations = Seq(
                RoomOccupancyReservationReport(
                  checkIn = LocalTime.of(0, 0),
                  checkOut = LocalTime.of(23, 59, 59)
                )
              )
            )
          )
        )
      )).provide(reporterLayer, roomManager, reservationManager)
    }
  )
