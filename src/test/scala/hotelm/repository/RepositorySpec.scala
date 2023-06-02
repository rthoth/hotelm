package hotelm.repository

import hotelm.Spec
import io.getquill.H2ZioJdbcContext
import io.getquill.SnakeCase

abstract class RepositorySpec extends Spec:
  protected val ctx = H2ZioJdbcContext(SnakeCase)
  export ctx.{Environment => _, *}
