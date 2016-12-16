package utils

import scala.concurrent.{Await, Future}
import generated.Tables._
import profile.api._
import slick.profile.BasicProfile
import scala.concurrent.duration.Duration

/**
  * Blocks until the future is done, implicitly
  */
object DbExecutionUtils {
  import scala.concurrent.ExecutionContext.Implicits.global

  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Returns the result of executing the action and retrieving the Future result
    * @param action The action to be executed
    * @param db The db to run under implicit parameter
    * @tparam E Concrete Entity result type
    * @tparam P Concrete Profile type
    * @return the result of executing the action and retrieving the Future result
    */
  implicit def exec[E, P <: BasicProfile](action: DBIO[E])(implicit db: P#Backend#Database) = Await.result(db.run(action), Duration.Inf)

  //------------------------------------------------------------------------
  /**
    * Returns the result of executing and waiting the given Future
    * @param f Future to execute and wait for
    * @tparam E concrete Entity result type
    * @return the result of executing and waiting the given Future
    */
  implicit def exec[E](f: Future[E]) = Await.result(f, Duration.Inf)
}