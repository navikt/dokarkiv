package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.feil.ForretningsmessigUnntak;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Unit tests of DefaultArkiverDokumentmottakFaultInfoPopulator.
 *
 * @author Stig Str�m
 */
public class ArkiverDokumentmottakFaultInfoPopulatorTest {

	private static final String KILDE = "JOARK";
	private static final String OPERATION_NAME = "testOperation";
	private static final String EXCEPTION_MESSAGE = "Exception message";

	private ArkiverDokumentmottakFaultInfoPopulator faultInfoPopulator;

	@Before
	public void setUp() {
		faultInfoPopulator = new ArkiverDokumentmottakFaultInfoPopulator();
	}

	@Test
	public void shouldPopulateFaultInfo() {
		TestForretningsmessigUnntak faultInfo = new TestForretningsmessigUnntak();

		Exception rootCause = new FileNotFoundException();
		Exception cause = new IOException(rootCause);
		Exception exception = new RuntimeException(EXCEPTION_MESSAGE, cause);

		faultInfoPopulator.populateFaultInfo(faultInfo, exception, OPERATION_NAME);

		assertThat(faultInfo.getFeilmelding(), is(EXCEPTION_MESSAGE));
		assertThat(faultInfo.getFeilkilde(), is(KILDE + ":" + OPERATION_NAME));
		assertThat(faultInfo.getFeilaarsak(), is(rootCause.toString()));
		assertThat(faultInfo.getTidspunkt().toGregorianCalendar().getTime().toString(), is(Date.from(LocalDateTime.now()
				.atZone(ZoneId.systemDefault())
				.toInstant()).toString()));
	}

	private static class TestForretningsmessigUnntak extends ForretningsmessigUnntak {

	}
}
