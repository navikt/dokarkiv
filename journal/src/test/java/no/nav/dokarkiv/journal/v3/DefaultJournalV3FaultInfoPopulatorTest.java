package no.nav.dokarkiv.journal.v3;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.journal.v3.feil.ForretningsmessigUnntak;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/**
 * Unit tests for DefaultJournalV3FaultInfoPopulator.
 *
 * @author Stig Strøm
 */
public class DefaultJournalV3FaultInfoPopulatorTest {

	private static final String KILDE = "JOARK";
	private static final String OPERATION_NAME = "testOperation";
	private static final String EXCEPTION_MESSAGE = "Exception message";

	private DefaultJournalV3FaultInfoPopulator faultInfoPopulator;

	@Before
	public void setUp() {
		faultInfoPopulator = new DefaultJournalV3FaultInfoPopulator();
		DateProvider.configure(true, DateProvider.getDate(new Date()));
	}

	@Test
	public void testFaultInfoPopulator() {
		TestForretningsmessigUnntak faultInfo = new TestForretningsmessigUnntak();

		Exception rootCause = new FileNotFoundException();
		Exception cause = new IOException(rootCause);
		Exception exception = new RuntimeException(EXCEPTION_MESSAGE, cause);

		faultInfoPopulator.populateFaultInfo(faultInfo, exception, OPERATION_NAME);

		assertThat(faultInfo.getFeilmelding(), is(EXCEPTION_MESSAGE));
		assertThat(faultInfo.getFeilkilde(), is(KILDE + ":" + OPERATION_NAME));
		assertThat(faultInfo.getFeilaarsak(), is(rootCause.toString()));
		assertThat(faultInfo.getTidspunkt().toGregorianCalendar().getTime(), is(DateProvider.getToday()));
	}

	private static class TestForretningsmessigUnntak extends ForretningsmessigUnntak {

	}

}