package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.feil.ForretningsmessigUnntak;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for DefaultBehandleJournalFaultInfoPopulator
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultBehandleJournalFaultInfoPopulatorTest {
	private static final String KILDE = "JOARK";
	private static final String OPERATION_NAME = "testOperation";
	private static final String EXCEPTION_MESSAGE = "Exception message";

	private DefaultBehandleJournalFaultInfoPopulator faultInfoPopulator;

	@BeforeEach
	public void setUp() {
		faultInfoPopulator = new DefaultBehandleJournalFaultInfoPopulator();
		DateProvider.configure(true, DateProvider.getDate(new Date()));
	}

	@AfterEach
	public void tearDown() {
		DateProvider.configure(false, null);
	}

	@Test
	public void shouldpopulateFaultInfoCorrectly() throws Exception {
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
