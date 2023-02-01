package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FjernFerdigstiltDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.KnyttDokumentTilJournalpostSomVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ArkiverDokumentproduksjonEndpoint
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class ArkiverDokumentproduksjonEndpointTest {

	@Mock
	private ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonProviderMock;

	@Mock
	private WebServiceContext webServiceContextMock;

	@Mock
	private Principal principalMock;

	@InjectMocks
	private ArkiverDokumentproduksjonEndpoint endpoint;

	@Test
	public void shouldDelegateToProviderForOpprettJournalpostArkiverDokument() {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
		OpprettJournalpostArkiverDokumentRequest request = new OpprettJournalpostArkiverDokumentRequest();
		OpprettJournalpostArkiverDokumentResponse response = new OpprettJournalpostArkiverDokumentResponse();
		when(arkiverDokumentproduksjonProviderMock.opprettJournalpostArkiverDokument(request)).thenReturn(response);

		OpprettJournalpostArkiverDokumentResponse wsReponse = endpoint.opprettJournalpostArkiverDokument(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForOpprettJournalpost() {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
		OpprettJournalpostRequest request = new OpprettJournalpostRequest();
		OpprettJournalpostResponse response = new OpprettJournalpostResponse();
		when(arkiverDokumentproduksjonProviderMock.opprettJournalpost(request)).thenReturn(response);

		OpprettJournalpostResponse wsReponse = endpoint.opprettJournalpost(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForOppdaterJournalpostArkiverDokument() throws Exception {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
		OppdaterJournalpostArkiverDokumentRequest request = new OppdaterJournalpostArkiverDokumentRequest();
		endpoint.oppdaterJournalpostArkiverDokument(request);
		verify(arkiverDokumentproduksjonProviderMock).oppdaterJournalpostArkiverDokument(request);
	}

	@Test
	public void shouldDelegateToArkiverVedlegg() throws Exception {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
		ArkiverVedleggRequest request = new ArkiverVedleggRequest();
		ArkiverVedleggResponse response = new ArkiverVedleggResponse();
		when(arkiverDokumentproduksjonProviderMock.arkiverVedlegg(request)).thenReturn(response);
		ArkiverVedleggResponse wsResponse = endpoint.arkiverVedlegg(request);
		assertThat(wsResponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForAvbrytJournalpost() throws Exception {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
		AvbrytJournalpostRequest request = new AvbrytJournalpostRequest();
		endpoint.avbrytJournalpost(request);
		verify(arkiverDokumentproduksjonProviderMock).avbrytJournalpost(request);
	}

	@Test
	public void shouldThrowUnsupportedExceptionForAvbrytVedlegg() {
		assertThrows(UnsupportedOperationException.class, () -> endpoint.avbrytVedlegg(new AvbrytVedleggRequest()));
	}

	@Test
	public void shouldThrowUnsupportedExceptionForFjernFerdigstiltDokument() {
		assertThrows(UnsupportedOperationException.class, () -> endpoint.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldThrowUnsupportedExceptionForSettDatoSendt() {
		assertThrows(UnsupportedOperationException.class, () -> endpoint.settDatoSendt(new SettDatoSendtRequest()));
	}

	@Test
	public void shouldThrowUnsupportedExceptionForKnyttDokumentTilJournalpostSomVedlegg() {
		assertThrows(UnsupportedOperationException.class, () -> endpoint.knyttDokumentTilJournalpostSomVedlegg(new KnyttDokumentTilJournalpostSomVedleggRequest()));
	}
}