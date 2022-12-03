package nl.wwbakker.eboekhouden.mapping

import eboekhouden.CMutatieFilter
import nl.wwbakker.eboekhouden.mapping.Helpers._

import java.time.{LocalDateTime, Month}

object Mutaties {

  private val beginDate = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0)
  private val endDateDate = LocalDateTime.of(2100, Month.JANUARY, 1, 0, 0)

  val leegFilter: CMutatieFilter = CMutatieFilter(
    MutatieNr = 0,
    MutatieNrVan = None,
    MutatieNrTm = None,
    Factuurnummer = None,
    DatumVan = beginDate.toXmlGregorianCalendar,
    DatumTm = endDateDate.toXmlGregorianCalendar
  )

}
