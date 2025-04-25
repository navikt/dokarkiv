package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.ForretningsmessigUnntak;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ArkiverDokumentproduksjonFaultInfoPopulatorTest {

	private static final String KILDE = "JOARK";
	private static final String OPERATION_NAME = "testOperation";
	private static final String EXCEPTION_MESSAGE = "Exception message";

	private DefaultArkiverDokumentproduksjonFaultInfoPopulator faultInfoPopulator;

	@BeforeEach
	public void setUp() {
		faultInfoPopulator = new DefaultArkiverDokumentproduksjonFaultInfoPopulator();
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
		Assertions.assertThat(faultInfo.getTidspunkt().toGregorianCalendar().toZonedDateTime().toLocalDateTime()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
	}

	private static class TestForretningsmessigUnntak extends ForretningsmessigUnntak {

	}
}
