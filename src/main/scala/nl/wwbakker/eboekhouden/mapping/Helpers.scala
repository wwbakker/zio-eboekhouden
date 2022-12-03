package nl.wwbakker.eboekhouden.mapping

import java.time.{LocalDateTime, ZoneId}
import java.util.GregorianCalendar
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

object Helpers {
  implicit class LocalDateTimeHelper(val dt: LocalDateTime) {
    private lazy val df = DatatypeFactory.newInstance()
    def toXmlGregorianCalendar: XMLGregorianCalendar = {
      new GregorianCalendar()
      val zdt = dt.atZone(ZoneId.systemDefault())
      val gc = GregorianCalendar.from(zdt)
      df.newXMLGregorianCalendar(gc)
    }
  }

}
