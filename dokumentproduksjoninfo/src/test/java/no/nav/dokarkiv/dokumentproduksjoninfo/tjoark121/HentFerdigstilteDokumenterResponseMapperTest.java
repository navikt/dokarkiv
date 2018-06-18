package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.informasjon.Dokument;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link HentFerdigstilteDokumenterResponseMapper}
 *
 * @author Stig Strøm
 */
public class HentFerdigstilteDokumenterResponseMapperTest {

	private static final String TITTEL = "brevtittel";
	private static final byte[] FIL_CONTENT = "fildata1".getBytes();
	private static final long DOKUMENT_INFO_ID = 42L;
	private HentFerdigstilteDokumenterResponseMapper responseMapper = new HentFerdigstilteDokumenterResponseMapper();
	
	@Test
	public void shouldMapToWsResponse() throws Exception {
		List<HentFerdigstilteDokumenterResponseTo> list = Arrays.asList(new HentFerdigstilteDokumenterResponseTo(
				DOKUMENT_INFO_ID, FIL_CONTENT, TITTEL));
		
		HentFerdigstilteDokumenterResponse wsResponse = responseMapper.map(list);
		
		assertThat(wsResponse.getDokumentListe().size(), is(1));
		assertThat(wsResponse.getDokumentListe().get(0).getDokumentInfoId(), is(DOKUMENT_INFO_ID));
		assertThat(wsResponse.getDokumentListe().get(0).getFil().getContentType(), is(MediaType.APPLICATION_PDF_VALUE));
		assertThat(getFil(wsResponse.getDokumentListe().get(0)), is(FIL_CONTENT));
		assertThat(wsResponse.getDokumentListe().get(0).getTittel(), is(TITTEL));
	}
	
	@Test
	public void shouldMapToWsResponse_emptyInputResponse() {
		List<HentFerdigstilteDokumenterResponseTo> domainResponse = new ArrayList<HentFerdigstilteDokumenterResponseTo>();
		HentFerdigstilteDokumenterResponse wsResponse = responseMapper.map(domainResponse);
		
		assertThat(wsResponse.getDokumentListe().size(), is(0));
	}
	
	
	private byte[] getFil(Dokument dokument) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		dokument.getFil().writeTo(output);
		return output.toByteArray();
	}

}
