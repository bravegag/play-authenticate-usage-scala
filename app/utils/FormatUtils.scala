package utils

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.DynamicVariable

object FormatUtils {
  //------------------------------------------------------------------------
  // public
  //------------------------------------------------------------------------
  implicit def formatTimestamp(timestamp: Long): String = {
    formatTimestamp(new Date(timestamp))
  }

  //------------------------------------------------------------------------
  implicit def formatTimestamp(timestamp: Date): String = {
    DateFormat.value match {
      case None => DateFormat.value = Some(new SimpleDateFormat("yyyy-dd-MM HH:mm:ss"))
      case _ => // ignore
    }
    DateFormat.value.map(_.format(timestamp)).get
  }

  //------------------------------------------------------------------------
  // members
  //------------------------------------------------------------------------
  val DateFormat = new DynamicVariable[Option[SimpleDateFormat]](None)
}