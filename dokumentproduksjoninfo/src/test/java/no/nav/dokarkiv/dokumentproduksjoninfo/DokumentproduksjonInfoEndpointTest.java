package no.nav.dokarkiv.dokumentproduksjoninfo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for DokumentproduksjonInfoEndpoint.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
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
	public void shouldDelegateToProviderFor_HentFerdigstilteDokumenter() throws Exception {
		HentFerdigstilteDokumenterRequest request = new HentFerdigstilteDokumenterRequest();
		HentFerdigstilteDokumenterResponse response = new HentFerdigstilteDokumenterResponse();
		when(dokumentproduksjonInfoProviderMock.hentFerdigstilteDokumenter(request)).thenReturn(response);
		
		HentFerdigstilteDokumenterResponse wsReponse = endpoint.hentFerdigstilteDokumenter(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForPing() throws Exception {
		endpoint.ping();

		verify(dokumentproduksjonInfoProviderMock).ping();
	}

}
