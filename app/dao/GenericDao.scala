package dao

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import scala.concurrent.Future

/**
  * Identifyable base for all Model types, it is also a Product
  * @tparam PK Primary key type
  */
trait Identifyable[PK] extends Product {
  //------------------------------------------------------------------------
  // members
  //------------------------------------------------------------------------
  def id : PK
}

/**
  * Generic DAO helper implementation
  */
trait GenericDaoHelper {
  val profile: slick.driver.JdbcProfile
  import profile.api._

  class GenericDao[PK, ER <: Identifyable[PK], ET <: Table[ER], TQ <: TableQuery[ET]] @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
      (tableQuery: TQ) extends HasDatabaseConfigProvider[JdbcProfile] {
    import driver.api._

    //------------------------------------------------------------------------
    // public
    //------------------------------------------------------------------------
    /**
      * Returns the row count for this Model
      * @return the row count for this Model
      */
    def count(): Future[Int] = db.run(tableQuery.length.result)

    //------------------------------------------------------------------------
    /**
      * Returns the matching entity for the given id
      * @param id identifier
      * @return the matching entity for the given id
      */
    def findById(id: PK): Future[Option[ER]] = db.run(tableQuery.filter(_.id === id).result.headOption)

    //------------------------------------------------------------------------
    /**
      * Returns all entities in this model
      * @return all entities in this model
      */
    def findAll(): Future[Seq[ER]] = db.run(tableQuery.result)

    //------------------------------------------------------------------------
    /**
      * Returns newly created entity with updated id
      * @param entity entity to create, input id is ignored
      * @return newly created entity with updated id
      */
    def create(entity: ER): Future[ER] = {
      val insertQuery = tableQuery returning tableQuery.map(_.id) into ((entity, id) => entity.copy(id = id))
      val action = insertQuery += entity
      db.run(action)
    }

    //------------------------------------------------------------------------
    /**
      * Returns number of inserted entities
      * @param entities to be inserted
      * @return number of inserted entities
      */
    def create(entities: Seq[ER]): Future[Int] = db.run(tableQuery ++= entities)

    //------------------------------------------------------------------------
    /**
      * Updates the given entity and returns a Future
      * @param update Entity to update (by id)
      * @return returns a Future
      */
    def update(update: ER): Future[Unit] = {
      db.run(tableQuery.filter(_.id === update.id).update(update)).map(_ => ()).map(_ => ())
    }

    //------------------------------------------------------------------------
    /**
      * Deletes the given entity by Id and returns a Future
      * @param id The Id to delete
      * @return returns a Future
      */
    def delete(id: PK): Future[Unit] = db.run(tableQuery.filter(_.id === id).delete).map(_ => ())
  }
}

