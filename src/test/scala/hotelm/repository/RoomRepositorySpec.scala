package hotelm.repository

import hotelm.Room
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
    }
  ).provideSome[Scope](
    H2DataSource.layer,
    ZLayer.fromZIO {
      for dataSource <- ZIO.service[DataSource]
      yield RoomRepository(ctx, ZLayer.succeed(dataSource))
    }
  )
