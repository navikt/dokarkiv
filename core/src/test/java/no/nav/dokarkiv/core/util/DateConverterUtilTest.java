package no.nav.dokarkiv.core.util;

import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DateConverterUtil}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class DateConverterUtilTest {

	private static final Date NOW = new Date();

	@Test
	public void convertXMLGregorianCalendarToDateShouldReturnNull() throws Exception {
		assertThat(DateConverterUtil.convertXMLGregorianCalendarToDate(null), is(nullValue()));
	}

	@Test
	public void shouldConvertXMLGregorianCalendarToDate() throws Exception {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.clear();
		gregorianCalendar.setTime(Date.from(LocalDate.of(2015, Month.MAY, 17).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
		xgc.setTime(2, 34, 56);

		assertThat(DateConverterUtil.convertXMLGregorianCalendarToDate(xgc),
				is((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2015-05-17 02:34:56"))));
	}

	@Test
	public void convertDateToXMLGregorianCalendarShouldReturnNull() throws Exception {
		assertThat(DateConverterUtil.convertDateToXMLGregorianCalendar(null), is(nullValue()));
	}

	@Test
	public void shouldConvertDateToXMLGregorianCalendar() throws Exception {
		XMLGregorianCalendar converted = DateConverterUtil.convertDateToXMLGregorianCalendar(NOW);

		assertThat(converted.toGregorianCalendar().getTime(), is(NOW));
	}

	@Test
	public void convertDateToXMLGregorianCalendarShouldThrowNullPointerException() throws Exception {
		assertThrows(NullPointerException.class, () -> DateConverterUtil.convertDateToXMLGregorianCalendar(null, false));
	}

	@Test
	public void shouldConvertDateToXMLGregorianCalendarWhenNullableFalse() throws Exception {
		XMLGregorianCalendar converted = DateConverterUtil.convertDateToXMLGregorianCalendar(NOW, false);

		assertThat(converted.toGregorianCalendar().getTime(), is(NOW));
	}

}