package hotelm.repository

import hotelm.Room
import hotelm.Spec
import io.getquill.*
import javax.sql.DataSource
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.*

object RoomRepositorySpec extends Spec:

  private val ctx = H2ZioJdbcContext(SnakeCase)
  import ctx.*

  override def spec = suite("RoomRepositorySpec")(
    test("It should add a new room.") {
      val expected = Room("42A", 2)
      for
        repository <- ZIO.service[RoomRepository]
        result     <- repository.add(expected)
        stored     <- ctx.run(quote(query[Room].filter(_.number == "42A").take(1)))
      yield assertTrue(
        result == expected,
        stored == List(expected)
      )
    },
    test("It should remove a room.") {
      val expected = Room("42A", 3)
      for
        repository <- ZIO.service[RoomRepository]
        _          <- ctx.run(quote(query[Room].insertValue(lift(expected))))
        result     <- repository.remove("42A")
        after      <- ctx.run(quote(query[Room].filter(_.number == "42A").take(1)))
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
