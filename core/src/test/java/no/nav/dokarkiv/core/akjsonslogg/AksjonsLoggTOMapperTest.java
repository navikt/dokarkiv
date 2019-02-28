package no.nav.dokarkiv.core.akjsonslogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggTOHeader;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggTOMapperTest {


	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private AksjonsLoggTOMapper aksjonsLoggTOMapper = new AksjonsLoggTOMapper();

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("username", "appId");

	}

	@Test
	public void shouldMap() throws IOException, UgyldigAksjonsLoggException {
		String aksjonsLoggHeaderString = objectToJsonString(createAksjonsLoggTOHeader());
		AksjonsLoggTO aksjonsLogg = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ARKIVERING, 1L, 1L);
		assertThat(aksjonsLogg.getBruker(), is(TestDataUtils.AKSJON_BRUKER));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ARKIVERING));
		assertThat(aksjonsLogg.getJournalpostId(), is(1L));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(1L));
	}

	@Test
	public void shouldMapNullWhenNull() throws IOException, UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTO = createAksjonsLoggTOHeader();
		aksjonsLoggTO.setMelding(null);
		aksjonsLoggTO.setBruker(null);
		String aksjonsLoggHeaderString = objectToJsonString(aksjonsLoggTO);
		AksjonsLoggTO aksjonsLogg = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ARKIVERING, 1L, 1L);
		assertThat(aksjonsLogg.getBruker(), nullValue());
		assertThat(aksjonsLogg.getMelding(), nullValue());
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ARKIVERING));
		assertThat(aksjonsLogg.getJournalpostId(), is(1L));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(1L));
	}

	@Test
	public void shouldThrowForInvalidAksjonsLoggHeaderString() throws UgyldigAksjonsLoggException {
		expectedException.expect(UgyldigAksjonsLoggException.class);
		aksjonsLoggTOMapper.mapAksjonsLoggHeader("{", AksjonsTypeCode.ARKIVERING, 1L, 1L);
	}
}