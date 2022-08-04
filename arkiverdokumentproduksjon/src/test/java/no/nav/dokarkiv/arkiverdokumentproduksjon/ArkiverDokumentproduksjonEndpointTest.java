package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FjernFerdigstiltDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.KnyttDokumentTilJournalpostSomVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doThrow;
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

	@BeforeEach
	public void setUp() {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
	}

	@Test
	public void shouldDelegateToProviderForOpprettJournalpostArkiverDokument() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = new OpprettJournalpostArkiverDokumentRequest();
		OpprettJournalpostArkiverDokumentResponse response = new OpprettJournalpostArkiverDokumentResponse();
		when(arkiverDokumentproduksjonProviderMock.opprettJournalpostArkiverDokument(request)).thenReturn(response);

		OpprettJournalpostArkiverDokumentResponse wsReponse = endpoint.opprettJournalpostArkiverDokument(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForOpprettJournalpost() throws Exception {
		OpprettJournalpostRequest request = new OpprettJournalpostRequest();
		OpprettJournalpostResponse response = new OpprettJournalpostResponse();
		when(arkiverDokumentproduksjonProviderMock.opprettJournalpost(request)).thenReturn(response);

		OpprettJournalpostResponse wsReponse = endpoint.opprettJournalpost(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForOppdaterJournalpostArkiverDokument() throws Exception {
		OppdaterJournalpostArkiverDokumentRequest request = new OppdaterJournalpostArkiverDokumentRequest();
		endpoint.oppdaterJournalpostArkiverDokument(request);
		verify(arkiverDokumentproduksjonProviderMock).oppdaterJournalpostArkiverDokument(request);
	}

	@Test
	public void shouldDelegateToArkiverVedlegg() throws Exception {
		ArkiverVedleggRequest request = new ArkiverVedleggRequest();
		ArkiverVedleggResponse response = new ArkiverVedleggResponse();
		when(arkiverDokumentproduksjonProviderMock.arkiverVedlegg(request)).thenReturn(response);
		ArkiverVedleggResponse wsResponse = endpoint.arkiverVedlegg(request);
		assertThat(wsResponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForAvbrytJournalpost() throws Exception {
		AvbrytJournalpostRequest request = new AvbrytJournalpostRequest();
		endpoint.avbrytJournalpost(request);
		verify(arkiverDokumentproduksjonProviderMock).avbrytJournalpost(request);
	}

	@Test
	public void shouldDelegateToProviderForAvbrytVedlegg() throws Exception {
		AvbrytVedleggRequest request = new AvbrytVedleggRequest();
		endpoint.avbrytVedlegg(request);
		verify(arkiverDokumentproduksjonProviderMock).avbrytVedlegg(request);
	}

	@Test
	public void shouldDelegateToProviderForFjernFerdigstiltDokument() throws Exception {
		FjernFerdigstiltDokumentRequest request = new FjernFerdigstiltDokumentRequest();
		endpoint.fjernFerdigstiltDokument(request);
		verify(arkiverDokumentproduksjonProviderMock).fjernFerdigstiltDokument(request);
	}

	@Test
	public void shouldDelegateToProviderForFerdigstillJournalpost() throws Exception {
		FerdigstillJournalpostRequest request = new FerdigstillJournalpostRequest();
		endpoint.ferdigstillJournalpost(request);
		verify(arkiverDokumentproduksjonProviderMock).ferdigstillJournalpost(request);
	}

	@Test
	public void shouldDelegateToProviderForSettDatoSendt() {
		SettDatoSendtRequest request = new SettDatoSendtRequest();

		endpoint.settDatoSendt(request);

		verify(arkiverDokumentproduksjonProviderMock).settDatoSendt(request);
	}

	@Test
	public void shouldDelegateToProviderForKnyttDokumentTilJournalpostSomVedlegg() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();

		endpoint.knyttDokumentTilJournalpostSomVedlegg(request);

		verify(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);
	}

	@Test
	public void throwsJournalpostIkkeFunnetExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet exception = new KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}

	@Test
	public void throwsJournalpostIkkeUnderArbeidExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid exception = new KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}

	@Test
	public void throwsJournalpostIkkeFerdigstiltExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt exception = new KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}

	@Test
	public void throwsDokumentIkkeFunnetExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet exception = new KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt exception = new KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}

	@Test
	public void throwsEksterneVedleggIkkeTillattExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt exception = new KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}

	@Test
	public void throwsUlikeFagomraaderExceptionWhenProviderDoes() throws Exception {
		KnyttDokumentTilJournalpostSomVedleggRequest request = new KnyttDokumentTilJournalpostSomVedleggRequest();
		KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader exception = new KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader();
		doThrow(exception).when(arkiverDokumentproduksjonProviderMock).knyttDokumentTilJournalpostSomVedlegg(request);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader.class,
				() -> endpoint.knyttDokumentTilJournalpostSomVedlegg(request));
	}
}