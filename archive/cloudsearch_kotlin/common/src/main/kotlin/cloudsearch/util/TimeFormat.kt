package cloudsearch.util

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat

// http://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html
object TimeFormat {
    val yyyMmDd = DateTimeFormat.forPattern("yyyy/MM/dd")
    val yyyyMmDdTHhMmSs= DateTimeFormat.forPattern("yyyy-MM-dd'THH:mm:ss")
    val yyyyMmDdTHhMmSsZ = ISODateTimeFormat.dateTime()
}