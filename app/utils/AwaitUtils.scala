package utils

import scala.concurrent.{Await, Future}
import generated.Tables._
import profile.api._
import scala.concurrent.duration.Duration

/**
  * Blocks until the future is done, implicitly
  */
object AwaitUtils {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  /**
    * Returns the result of executing the action and retrieving the Future result
    * @param action The action to be executed
    * @param db The db to run under implicit parameter
    * @tparam E Concrete Entity result type
    * @return the result of executing the action and retrieving the Future result
    */
  implicit def await[E](action: DBIO[E])(implicit db: Database) =
    Await.result(db.run(action), Duration.Inf)

  //------------------------------------------------------------------------
  /**
    * Returns the result of executing and waiting the given Future
    * @param f Future to execute and wait for
    * @tparam E concrete Entity result type
    * @return the result of executing and waiting the given Future
    */
  implicit def await[E](f: Future[E]) = Await.result(f, Duration.Inf)
}