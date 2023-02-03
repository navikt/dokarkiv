package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDocumentUpdateException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentResponseMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102.OppdaterJournalpostArkiverDokumentRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102.OppdaterJournalpostArkiverDokumentRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102.OppdaterJournalpostArkiverDokumentService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103.AvbrytJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103.AvbrytJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggResponseMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AlleredeFerdigstiltException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostAvbrytelseIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FeilStrukturException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostInneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KanIkkeFerdigstillesException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ObjektIkkeFunnetException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.UgyldigInputException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ArkiverDokumentproduksjonProvider
 */
@ExtendWith(MockitoExtension.class)
public class ArkiverDokumentproduksjonProviderTest {

	private static final String ENDRET_AV_NAVN = "endretAvNavn";
	private static final Long JOURNALPOST_ID = 37483L;
	private static final Long DOCUMENT_INFO_ID = 2433L;

	@Mock
	private OpprettJournalpostArkiverDokumentRequestMapper opprettJournalpostArkiverDokumentRequestMapperMock;

	@Mock
	private OpprettJournalpostArkiverDokumentResponseMapper opprettJournalpostArkiverDokumentResponseMapperMock;

	@Mock
	private OpprettJournalpostArkiverDokumentService opprettJournalpostArkiverDokumentServiceMock;

	@Mock
	private OpprettJournalpostRequestMapper opprettJournalpostRequestMapperMock;

	@Mock
	private OpprettJournalpostService opprettJournalpostServiceMock;

	@Mock
	private OppdaterJournalpostArkiverDokumentService oppdaterJournalpostArkiverDokumentServiceMock;

	@Mock
	private OppdaterJournalpostArkiverDokumentRequestMapper oppdaterJournalpostArkiverDokumentRequestMapperMock;

	@Mock
	private AvbrytJournalpostService avbrytJournalpostServiceMock;

	@Mock
	private ArkiverDokumentproduksjonFaultInfoPopulator faultInfoPopulatorMock;

	@Mock
	private ArkiverVedleggService arkiverVedleggServiceMock;

	@Mock
	private ArkiverVedleggRequestMapper arkiverVedleggRequestMapperMock;

	@Mock
	private ArkiverVedleggResponseMapper arkiverVedleggResponseMapperMock;

	@Mock
	private FerdigstillJournalpostService ferdigstillJournalpostServiceMock;

	@Mock
	private FerdigstillJournalpostRequestMapper ferdigstillJournalpostRequestMapperMock;

	@InjectMocks
	private ArkiverDokumentproduksjonProvider provider;

	@Test
	public void shouldOpprettJournalpostArkiverDokument() {
		OpprettJournalpostArkiverDokumentResponse wsResponse =
				new OpprettJournalpostArkiverDokumentResponse();
		wsResponse.setJournalpostId(JOURNALPOST_ID);

		when(opprettJournalpostArkiverDokumentServiceMock.opprettJournalpostArkiverDokument(any())).thenReturn(OpprettJournalpostArkiverDokumentResponseTo
				.builder()
				.dokumentInfoId(DOCUMENT_INFO_ID)
				.journalpostId(JOURNALPOST_ID)
				.build());

		when(opprettJournalpostArkiverDokumentResponseMapperMock
				.map(any()))
				.thenReturn(wsResponse);

		OpprettJournalpostArkiverDokumentResponse response = provider
				.opprettJournalpostArkiverDokument(new OpprettJournalpostArkiverDokumentRequest());

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
	}

	@Test
	public void shouldOpprettJournalpost() {
		OpprettJournalpostRequest wsRequest = new OpprettJournalpostRequest();

		OpprettJournalpostResponseTo domainResponse = OpprettJournalpostResponseTo.builder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoId(DOCUMENT_INFO_ID)
				.build();
		when(opprettJournalpostServiceMock
				.opprettJournalpost(any()))
				.thenReturn(domainResponse);

		OpprettJournalpostResponse response = provider.opprettJournalpost(wsRequest);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoId(), is(DOCUMENT_INFO_ID));
	}

	@Test
	public void shouldOppdaterJournalpostArkiverDokument() throws UgyldigInputException, AlleredeFerdigstiltException, ObjektIkkeFunnetException, FeilStrukturException, KanIkkeFerdigstillesException {
		OppdaterJournalpostArkiverDokumentRequestTo domainRequest = OppdaterJournalpostArkiverDokumentRequestTo.builder().build();
		when(oppdaterJournalpostArkiverDokumentRequestMapperMock
				.map(any()))
				.thenReturn(domainRequest);

		OppdaterJournalpostArkiverDokumentRequest wsRequest = new OppdaterJournalpostArkiverDokumentRequest();
		wsRequest.setJournalpostId(1L);

		provider.oppdaterJournalpostArkiverDokument(wsRequest);

		verify(oppdaterJournalpostArkiverDokumentRequestMapperMock)
				.map(any(OppdaterJournalpostArkiverDokumentRequest.class));
		verify(oppdaterJournalpostArkiverDokumentServiceMock)
				.oppdaterJournalpostArkiverDokument(domainRequest);
	}

	@Test
	public void shouldAvbrytJournalpost() throws AvbrytJournalpostAvbrytelseIkkeTillatt, AvbrytJournalpostJournalpostIkkeFunnet, AvbrytJournalpostJournalpostAlleredeAvbrutt {
		provider.avbrytJournalpost(createAvbrytJournalpostRequest(JOURNALPOST_ID, ENDRET_AV_NAVN));
		verify(avbrytJournalpostServiceMock).avbrytJournalpost(argThat(new IsAvbrytJournalpostServiceCalledWithExpectedInput()));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostNotFound() {
		doThrow(new NoJournalpostFoundException("Not found", JOURNALPOST_ID)).when(avbrytJournalpostServiceMock)
				.avbrytJournalpost(any(AvbrytJournalpostRequestTo.class));

		assertThrows(AvbrytJournalpostJournalpostIkkeFunnet.class,
				() -> provider.avbrytJournalpost(createAvbrytJournalpostRequest(JOURNALPOST_ID, ENDRET_AV_NAVN)));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostAlleredeAvbrutt() {
		UgyldigJournalStatusOvergangException alleredeAvbrutt =
				new UgyldigJournalStatusOvergangException("Allerede Avbrutt", JournalStatusCode.A, JournalStatusCode.A, JournalpostTypeCode.I);
		doThrow(alleredeAvbrutt).when(avbrytJournalpostServiceMock).avbrytJournalpost(any(AvbrytJournalpostRequestTo.class));

		assertThrows(AvbrytJournalpostJournalpostAlleredeAvbrutt.class,
				() -> provider.avbrytJournalpost(createAvbrytJournalpostRequest(JOURNALPOST_ID, ENDRET_AV_NAVN)));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostStatusHasUgyldigOvergang() {
		UgyldigJournalStatusOvergangException ugyldigOvergang =
				new UgyldigJournalStatusOvergangException("Kan ikke fremprovosere transisjon fra journalført til avbrutt", JournalStatusCode.J, JournalStatusCode.A, JournalpostTypeCode.I);
		doThrow(ugyldigOvergang).when(avbrytJournalpostServiceMock).avbrytJournalpost(any(AvbrytJournalpostRequestTo.class));

		assertThrows(AvbrytJournalpostAvbrytelseIkkeTillatt.class,
				() -> provider.avbrytJournalpost(createAvbrytJournalpostRequest(JOURNALPOST_ID, ENDRET_AV_NAVN)));
	}

	@Test
	public void shouldArkiverVedlegg() throws ArkiverVedleggJournalpostIkkeFunnet, ArkiverVedleggJournalpostIkkeUnderArbeid {
		when(arkiverVedleggRequestMapperMock.map(any())).thenReturn(new ArkiverVedleggRequestTo());
		when(arkiverVedleggServiceMock.arkiverVedlegg(any())).thenReturn(ArkiverVedleggResponseTo.builder()
				.dokumentInfoId(12L)
				.journalpostId(11L)
				.build());
		provider.arkiverVedlegg(createArkiverVedleggRequest(JOURNALPOST_ID));
		verify(arkiverVedleggServiceMock).arkiverVedlegg(any(ArkiverVedleggRequestTo.class));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsNull() throws NoJournalpostFoundException {
		doThrow(new NoJournalpostFoundException("Journalpost not found", JOURNALPOST_ID)).when(arkiverVedleggServiceMock)
				.arkiverVedlegg(any());

		assertThrows(ArkiverVedleggJournalpostIkkeFunnet.class,
				() -> provider.arkiverVedlegg(createArkiverVedleggRequest(null)));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsIkkeUnderArbeid() throws NoJournalpostFoundException, ArkiverVedleggJournalpostIkkeUnderArbeid, ArkiverVedleggJournalpostIkkeFunnet {
		doThrow(new IllegalDocumentUpdateException("Journalpost with id: " + JOURNALPOST_ID + " can not be updated")).when(arkiverVedleggServiceMock)
				.arkiverVedlegg(any());

		assertThrows(ArkiverVedleggJournalpostIkkeUnderArbeid.class,
				() -> provider.arkiverVedlegg(createArkiverVedleggRequest(JOURNALPOST_ID)));
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptionWhenSettDatoSendt() {
		assertThrows(UnsupportedOperationException.class, () -> provider.settDatoSendt(new SettDatoSendtRequest()), "sanert");
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptionWhenFjernFerdigstiltDokument() {
		assertThrows(UnsupportedOperationException.class, () -> provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()), "sanert");
	}

	@Test
	public void shouldFerdigstillJournalpost() throws FerdigstillJournalpostJournalpostIkkeFunnet, FerdigstillJournalpostInneholderDokumenterUnderRedigering, FerdigstillJournalpostJournalpostIkkeUnderArbeid {
		when(ferdigstillJournalpostRequestMapperMock.map(any(FerdigstillJournalpostRequest.class))).thenReturn(
				new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN, UtsendingsKanalCode.EESSI));
		provider.ferdigstillJournalpost(new FerdigstillJournalpostRequest());
		verify(ferdigstillJournalpostServiceMock).ferdigstillJournalpost(any(FerdigstillJournalpostRequestTo.class));
	}

	@Test
	public void shouldThrowException_FerdigstillJournalpostJournalpostIkkeFunnet() {
		doThrow(new NoJournalpostFoundException("Cannot find", JOURNALPOST_ID)).when(ferdigstillJournalpostServiceMock)
				.ferdigstillJournalpost(any());

		assertThrows(FerdigstillJournalpostJournalpostIkkeFunnet.class,
				() -> provider.ferdigstillJournalpost(new FerdigstillJournalpostRequest()));
	}

	@Test
	public void shouldThrowException_FerdigstillJournalpostJournalpostIkkeUnderArbeid() {
		doThrow(new UgyldigJournalStatusVerdiException("Cannot find", null)).when(ferdigstillJournalpostServiceMock)
				.ferdigstillJournalpost(any());

		assertThrows(FerdigstillJournalpostJournalpostIkkeUnderArbeid.class,
				() -> provider.ferdigstillJournalpost(new FerdigstillJournalpostRequest()));
	}

	@Test
	public void shouldThrowException_FerdigstillJournalpostInneholderDokumenterUnderRedigering() {
		doThrow(new UgyldigDokumentStatusVerdiException("journal status", null))
				.when(ferdigstillJournalpostServiceMock).ferdigstillJournalpost(any());

		assertThrows(FerdigstillJournalpostInneholderDokumenterUnderRedigering.class,
				() -> provider.ferdigstillJournalpost(new FerdigstillJournalpostRequest()));
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptionWhenAvbrytVedlegg() {
		assertThrows(UnsupportedOperationException.class, () -> provider.avbrytVedlegg(new AvbrytVedleggRequest()), "sanert");
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptionKnyttDokumentTilJournalpostSomVedlegg() {
		assertThrows(UnsupportedOperationException.class, () -> provider.knyttDokumentTilJournalpostSomVedlegg(new KnyttDokumentTilJournalpostSomVedleggRequest()), "sanert");
	}

	private AvbrytJournalpostRequest createAvbrytJournalpostRequest(long journalpostId, String endretAvNavn) {
		AvbrytJournalpostRequest avbrytJournalpostRequest = new AvbrytJournalpostRequest();
		avbrytJournalpostRequest.setEndretAvNavn(endretAvNavn);
		avbrytJournalpostRequest.setJournalpostId(journalpostId);
		return avbrytJournalpostRequest;
	}

	private ArkiverVedleggRequest createArkiverVedleggRequest(Long journalpostId) {
		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();

		if (journalpostId != null) {
			Journalpost journalpost = new Journalpost();
			journalpost.setJournalpostId(journalpostId.toString());
			journalpost.setEndretAvNavn(ENDRET_AV_NAVN);
			journalpost.setDokumentInfo(new DokumentInfo());
			arkiverVedleggRequest.setJournalpost(journalpost);
		}
		arkiverVedleggRequest.setFerdigstillDokument(false);

		return arkiverVedleggRequest;
	}

	/**
	 * Verifies if AvbrytJournalpost is called with expected values;
	 *
	 * @author Stig Strøm
	 */
	private class IsAvbrytJournalpostServiceCalledWithExpectedInput implements ArgumentMatcher<AvbrytJournalpostRequestTo> {

		@Override
		public boolean matches(AvbrytJournalpostRequestTo requestTo) {
			return requestTo.getJournalpostId() == JOURNALPOST_ID.longValue()
				   && ENDRET_AV_NAVN.equals(requestTo.getEndretAvNavn());
		}
	}
}