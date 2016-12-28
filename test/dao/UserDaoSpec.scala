package dao

import com.google.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import play.api.db.Database

class UserDaoSpec @Inject()(@NamedDatabase("test") dbConfigProvider: DatabaseConfigProvider,
                            @NamedDatabase("test") db: Database) extends FunSpecDao(db) {
  describe("Testing") {
    it("what we do") {
      assert(!("did it run?????????".isEmpty))
    }
  }
}
