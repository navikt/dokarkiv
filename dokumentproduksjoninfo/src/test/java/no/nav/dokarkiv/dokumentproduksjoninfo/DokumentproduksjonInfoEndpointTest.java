package no.nav.dokarkiv.dokumentproduksjoninfo;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DokumentproduksjonInfoEndpoint.
 */
@ExtendWith(MockitoExtension.class)
public class DokumentproduksjonInfoEndpointTest {

	@Mock
	private DokumentproduksjonInfoV1 dokumentproduksjonInfoProviderMock;

	@InjectMocks
	private DokumentproduksjonInfoEndpoint endpoint;

	@Test
	public void shouldDelegateToProviderFor_HentJournalOgDokumentStatus() throws Exception {
		HentJournalOgDokumentStatusRequest request = new HentJournalOgDokumentStatusRequest();
		HentJournalOgDokumentStatusResponse response = new HentJournalOgDokumentStatusResponse();
		when(dokumentproduksjonInfoProviderMock.hentJournalOgDokumentStatus(request)).thenReturn(response);

		HentJournalOgDokumentStatusResponse wsReponse = endpoint.hentJournalOgDokumentStatus(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldThrowUnsupportedExceptionForHentFerdigstilteDokumenter() throws Exception {
		assertThrows(UnsupportedOperationException.class,
				() -> endpoint.hentFerdigstilteDokumenter(new HentFerdigstilteDokumenterRequest()), "sanert");
	}

	@Test
	public void shouldThrowUnsupportedExceptionForHentJournalpostInfo() throws Exception {
		assertThrows(UnsupportedOperationException.class,
				() -> endpoint.hentJournalpostInfo(new HentJournalpostInfoRequest()), "sanert");
	}

	@Test
	public void shouldDelegateToProviderForPing() throws Exception {
		endpoint.ping();

		verify(dokumentproduksjonInfoProviderMock).ping();
	}

}
