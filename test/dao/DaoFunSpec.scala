package dao

import org.scalatest.{BeforeAndAfter, FunSpec}
import play.api.db.evolutions.Evolutions
import play.api.db.Database

abstract class DaoFunSpec(db: Database) extends FunSpec with BeforeAndAfter {
  before {
    Evolutions.applyEvolutions(db)
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }
}
