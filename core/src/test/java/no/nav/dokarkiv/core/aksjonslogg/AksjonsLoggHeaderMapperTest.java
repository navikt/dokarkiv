package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggRequest;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggRequestAksjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggHeaderMapperTest {



	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper = new AksjonsLoggHeaderMapper();
	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("username", "appId");

	}

	@Test
	public void shouldMap() throws IOException, UgyldigAksjonsLoggHeaderException {
		String aksjonsLoggHeaderString = objectToJsonString(Arrays.asList(
				createAksjonsLoggRequest(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name()),
				createAksjonsLoggRequest(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name())
				));
		List<AksjonsLoggHeader> aksjonsLoggHeaderListe = aksjonsLoggHeaderMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString);
		assertThat(aksjonsLoggHeaderListe.size(), is(2));
		AksjonsLoggHeader aksjonsLogg = aksjonsLoggHeaderListe.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING.name()));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getApplikasjon(), is(TestDataUtils.AKSJON_APPLIKASJON));
		assertThat(aksjonsLogg.getBruker(), is(TestDataUtils.AKSJON_BRUKER));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(1L));
		assertThat(aksjonsLogg.getJournalpostId(), is(1L));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getArkivElement(), is(TestDataUtils.AKSJON_ARKIVELEMENT));
		assertThat(aksjonsLogg.getFraVerdi(), is(TestDataUtils.AKSJON_FRA_VERDI));
		assertThat(aksjonsLogg.getTilVerdi(), is(TestDataUtils.AKSJON_TIL_VERDI));
	}

	@Test
	public void shouldThrowForInvalidAksjonsLoggHeader() throws UgyldigAksjonsLoggHeaderException {
		expectedException.expect(UgyldigAksjonsLoggHeaderException.class);
		expectedException.expectMessage("Sjekk om headeren er i gyldig JSON format.");
		aksjonsLoggHeaderMapper.mapAksjonsLoggHeader("asdsad");
	}
}