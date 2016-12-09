package utils

import scala.collection.JavaConversions

object PlayConversions {
  /**
    * Returns the result conversion from a play.api.mvc.Session to a play.mvc.Http.Session
    * @param session play.api.mvc.Session instance
    * @return the result conversion from a play.api.mvc.Session to a play.mvc.Http.Session
    */
  implicit def toHttpSession(session: play.api.mvc.Session) = new play.mvc.Http.Session(JavaConversions.mapAsJavaMap(session.data))
}