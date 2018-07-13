package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Unit tests for {@link DateConverterUtil}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Ignore
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
//		gregorianCalendar.setTime(createDate(2015, Calendar.MAY, 17));
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

	@Test(expected = NullPointerException.class)
	public void convertDateToXMLGregorianCalendarShouldThrowNullPointerException() throws Exception {
		DateConverterUtil.convertDateToXMLGregorianCalendar(null, false);
	}

	@Test
	public void shouldConvertDateToXMLGregorianCalendarWhenNullableFalse() throws Exception {
		XMLGregorianCalendar converted = DateConverterUtil.convertDateToXMLGregorianCalendar(NOW, false);

		assertThat(converted.toGregorianCalendar().getTime(), is(NOW));
	}

}