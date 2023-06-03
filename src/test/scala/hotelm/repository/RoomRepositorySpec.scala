package hotelm.repository

import hotelm.Room
import hotelm.fixture.RoomFixture
import io.getquill.*
import javax.sql.DataSource
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.*

object RoomRepositorySpec extends RepositorySpec:

  override def spec = suite("RoomRepositorySpec")(
    test("It should add a new room.") {
      val expected = Room("42A", 2)
      for
        repository <- ZIO.service[RoomRepository]
        result     <- repository.add(expected)
        stored     <- run(quote(query[Room].filter(_.number == "42A").take(1)))
      yield assertTrue(
        result == expected,
        stored == List(expected)
      )
    },
    test("It should remove a room.") {
      val expected = Room("42A", 3)
      for
        repository <- ZIO.service[RoomRepository]
        _          <- run(quote(query[Room].insertValue(lift(expected))))
        result     <- repository.remove("42A")
        after      <- run(quote(query[Room].filter(_.number == "42A").take(1)))
      yield assertTrue(
        result.contains(expected),
        after.isEmpty
      )
    },
    test("It should get a specific room.") {
      val room = RoomFixture.createNew()
      for
        _      <- run(quote(query[Room].insertValue(lift(room))))
        result <- ZIO.serviceWithZIO[RoomRepository](_.get(room.number))
      yield assertTrue(
        result.contains(room)
      )
    },
    test("It should get all rooms.") {
      val rooms       = for (_ <- 0 until 13) yield RoomFixture.createNew()
      val insertQuery = quote(liftQuery(rooms).foreach(room => query[Room].insertValue(room)))
      for
        _      <- run(insertQuery)
        result <- ZIO.serviceWithZIO[RoomRepository](_.all)
      yield assertTrue(
        result == rooms
      )
    }
  ).provideSome[Scope](
    H2DataSource.layer,
    ZLayer.fromZIO {
      for dataSource <- ZIO.service[DataSource]
      yield RoomRepository(ctx, ZLayer.succeed(dataSource))
    }
  )
