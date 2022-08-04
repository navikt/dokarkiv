package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoInnskrenketPartsinnsynException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoIsOrganInterntException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoSlettetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilregistrertSaksrelasjonException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FilDetaljerOnDemandException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDocumentUpdateException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalFagomraadeException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalTilleggsopplysningerException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostIkkeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostNotFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
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
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104.SettDatoSendtRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104.SettDatoSendtRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104.SettDatoSendtService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggResponseMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106.AvbrytVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106.AvbrytVedleggService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107.FjernFerdigstiltDokumentRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107.FjernFerdigstiltDokumentService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109.KnyttDokumentTilJournalpostSomVedleggRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109.KnyttDokumentTilJournalpostSomVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109.KnyttDokumentTilJournalpostSomVedleggService;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AlleredeFerdigstiltException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostAvbrytelseIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentIkkeVedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FeilStrukturException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostInneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentAlleredeRedigerbart;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KanIkkeFerdigstillesException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader;
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
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ArkiverDokumentproduksjonProvider
 *
 * @author Joakim Bjørnstad, Visma Consulting
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
	private SettDatoSendtRequestMapper settDatoSendtRequestMapperMock;

	@Mock
	private SettDatoSendtService settDatoSendtServiceMock;

	@Mock
	private ArkiverDokumentproduksjonFaultInfoPopulator faultInfoPopulatorMock;

	@Mock
	private ArkiverVedleggService arkiverVedleggServiceMock;

	@Mock
	private ArkiverVedleggRequestMapper arkiverVedleggRequestMapperMock;

	@Mock
	private ArkiverVedleggResponseMapper arkiverVedleggResponseMapperMock;

	@Mock
	private FjernFerdigstiltDokumentService fjernFerdigstiltDokumentServiceMock;

	@Mock
	private AvbrytVedleggService avbrytVedleggServiceMock;

	@Mock
	private FerdigstillJournalpostService ferdigstillJournalpostServiceMock;

	@Mock
	private FerdigstillJournalpostRequestMapper ferdigstillJournalpostRequestMapperMock;

	@Mock
	private KnyttDokumentTilJournalpostSomVedleggService knyttDokumentTilJournalpostSomVedleggServiceMock;

	@Mock
	private KnyttDokumentTilJournalpostSomVedleggRequestMapper knyttDokumentTilJournalpostSomVedleggRequestMapperMock;

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
	public void shouldSettDatoSendt() {
		SettDatoSendtRequest wsRequest = new SettDatoSendtRequest();
		SettDatoSendtRequestTo domainRequest = new SettDatoSendtRequestTo(null, null, null);
		when(settDatoSendtRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);

		provider.settDatoSendt(wsRequest);

		verify(settDatoSendtServiceMock).settDatoSendt(domainRequest);
	}

	@Test
	public void shouldFjernFerdigstiltDokument() throws FjernFerdigstiltDokumentDokumentAlleredeRedigerbart, FjernFerdigstiltDokumentJournalpostIkkeFunnet, FjernFerdigstiltDokumentDokumentIkkeFunnet, FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid, FjernFerdigstiltDokumentDokumentAlleredeAvbrutt {
		provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest());
		verify(fjernFerdigstiltDokumentServiceMock).fjernFerdigstiltDokument(any(FjernFerdigstiltDokumentRequestTo.class));
	}

	@Test
	public void shouldThrowException_FjernFerdigstiltDokumentJournalpostIkkeFunnet() {
		doThrow(new NoJournalpostFoundException("Cannot find", JOURNALPOST_ID)).when(fjernFerdigstiltDokumentServiceMock)
				.fjernFerdigstiltDokument(any(FjernFerdigstiltDokumentRequestTo.class));

		assertThrows(FjernFerdigstiltDokumentJournalpostIkkeFunnet.class,
				() -> provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldThrowException_FjernFerdigstiltDokumentDokumentIkkeFunnet() {
		doThrow(new NoDokumentInfoFoundException("Cannot find", DOCUMENT_INFO_ID)).when(fjernFerdigstiltDokumentServiceMock)
				.fjernFerdigstiltDokument(any(FjernFerdigstiltDokumentRequestTo.class));

		assertThrows(FjernFerdigstiltDokumentDokumentIkkeFunnet.class,
				() -> provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldThrowException_FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid() {
		doThrow(new UgyldigJournalStatusVerdiException("journal status", JournalStatusCode.FL))
				.when(
						fjernFerdigstiltDokumentServiceMock)
				.fjernFerdigstiltDokument(any(FjernFerdigstiltDokumentRequestTo.class));

		assertThrows(FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid.class,
				() -> provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldThrowException_FjernFerdigstiltDokumentDokumentAlleredeRedigerbart() {
		doThrow(new UgyldigDokumentStatusVerdiException("dokument status", DokumentStatusCode.UNDER_REDIGERING))
				.when(
						fjernFerdigstiltDokumentServiceMock)
				.fjernFerdigstiltDokument(any(FjernFerdigstiltDokumentRequestTo.class));

		assertThrows(FjernFerdigstiltDokumentDokumentAlleredeRedigerbart.class,
				() -> provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldThrowException_FjernFerdigstiltDokumentDokumentAlleredeAvbrutt() {
		doThrow(new UgyldigDokumentStatusVerdiException("dokument status", DokumentStatusCode.AVBRUTT))
				.when(
						fjernFerdigstiltDokumentServiceMock)
				.fjernFerdigstiltDokument(any(FjernFerdigstiltDokumentRequestTo.class));

		assertThrows(FjernFerdigstiltDokumentDokumentAlleredeAvbrutt.class,
				() -> provider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
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
	public void shouldThrowExceptionAvbrytVedleggWsRequestIsNull() {
		assertThrows(IllegalArgumentException.class,
				() -> provider.avbrytVedlegg(null),
				"Request is null");
	}

	@Test
	public void shouldAvbrytVedlegg() throws AvbrytVedleggJournalpostIkkeFunnet, AvbrytVedleggDokumentIkkeFunnet, AvbrytVedleggDokumentIkkeVedlegg, AvbrytVedleggJournalpostIkkeUnderArbeid, AvbrytVedleggDokumentAlleredeAvbrutt {
		provider.avbrytVedlegg(createAvbrytVedlegRequest());

		ArgumentCaptor<AvbrytVedleggRequestTo> captor = ArgumentCaptor.forClass(AvbrytVedleggRequestTo.class);
		verify(avbrytVedleggServiceMock).avbrytVedlegg(captor.capture());
		AvbrytVedleggRequestTo requestTo = captor.getValue();

		assertThat(requestTo.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(requestTo.getDokumentInfoId(), is(DOCUMENT_INFO_ID));
		assertThat(requestTo.getEndretAvNavn(), is(ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowAvbrytVedleggJournalpostIkkeFunnet() {
		doThrow(new NoJournalpostFoundException("Cannot find", JOURNALPOST_ID)).when(avbrytVedleggServiceMock)
				.avbrytVedlegg(any(AvbrytVedleggRequestTo.class));

		assertThrows(AvbrytVedleggJournalpostIkkeFunnet.class,
				() -> provider.avbrytVedlegg(new AvbrytVedleggRequest()));
	}

	@Test
	public void shouldThrowAvbrytVedleggDokumentIkkeFunnet() {
		doThrow(new NoDokumentInfoFoundException("Cannot find", DOCUMENT_INFO_ID)).when(avbrytVedleggServiceMock)
				.avbrytVedlegg(any(AvbrytVedleggRequestTo.class));

		assertThrows(AvbrytVedleggDokumentIkkeFunnet.class,
				() -> provider.avbrytVedlegg(new AvbrytVedleggRequest()));
	}

	@Test
	public void shouldThrowAvbrytVedleggJournalpostIkkeUnderArbeid() {
		doThrow(new UgyldigJournalStatusVerdiException("journal status", JournalStatusCode.FL)).when(avbrytVedleggServiceMock)
				.avbrytVedlegg(any(AvbrytVedleggRequestTo.class));

		assertThrows(AvbrytVedleggJournalpostIkkeUnderArbeid.class,
				() -> provider.avbrytVedlegg(new AvbrytVedleggRequest()));
	}

	@Test
	public void shouldThrowAvbrytVedleggDokumentAlleredeAvbrutt() {
		doThrow(new UgyldigDokumentStatusVerdiException("dokument status", DokumentStatusCode.AVBRUTT)).when(avbrytVedleggServiceMock)
				.avbrytVedlegg(any(AvbrytVedleggRequestTo.class));

		assertThrows(AvbrytVedleggDokumentAlleredeAvbrutt.class,
				() -> provider.avbrytVedlegg(new AvbrytVedleggRequest()));
	}

	@Test
	public void shouldThrowAvbrytVedleggDokumentIkkeVedlegg() {
		doThrow(new UgyldigTilknyttetJournalpostSomVerdiException("Tilknyttet som", TilknyttetJournalpostSomCode.HOVEDDOKUMENT))
				.when(avbrytVedleggServiceMock)
				.avbrytVedlegg(any(AvbrytVedleggRequestTo.class));

		assertThrows(AvbrytVedleggDokumentIkkeVedlegg.class,
				() -> provider.avbrytVedlegg(new AvbrytVedleggRequest()));
	}

	@Test
	public void doesNothingWhenKnyttDokumentTilJournalpostSomVedleggSucceeds() throws KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet, KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader, KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt, KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		provider.knyttDokumentTilJournalpostSomVedlegg(requestMock);
	}

	@Test
	public void knyttDokumentTilJournalpostSomVedleggCallsMapperWithRequest() throws KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet, KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader, KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt, KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		provider.knyttDokumentTilJournalpostSomVedlegg(requestMock);

		verify(knyttDokumentTilJournalpostSomVedleggRequestMapperMock).map(requestMock);
	}

	@Test
	public void knyttDokumentTilJournalpostSomVedleggCallsServiceWithMappedRequest() throws KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet, KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader, KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt, KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet, KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		provider.knyttDokumentTilJournalpostSomVedlegg(requestMock);

		verify(knyttDokumentTilJournalpostSomVedleggServiceMock).knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktWhenServiceThrowsDokumentInfoInnskrenketPartsinnsynException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new DokumentInfoInnskrenketPartsinnsynException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktWhenServiceThrowsDokumentInfoSlettetException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new DokumentInfoSlettetException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktWhenServiceThrowsDokumentInfoIsOrganInterntException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new DokumentInfoIsOrganInterntException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktWhenServiceThrowsIllegalDokumentstatusException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new IllegalDokumentstatusException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktWhenServiceThrowsFilDetaljerOnDemandException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new FilDetaljerOnDemandException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsDokumentTillatesIkkeGjenbruktWhenServiceThrowsIllegalVariantFormatException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new IllegalVariantFormatException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsJournalpostIkkeFunnetWhenServiceThrowsJournalpostNotFoundException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new JournalpostNotFoundException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsDokumentIkkeFunnetWhenServiceThrowsDokumentInfoNotFoundException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new DokumentInfoNotFoundException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsUlikeFagomraaderWhenServiceThrowsIllegalFagomraadeException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new IllegalFagomraadeException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsJournalpostIkkeFerdigstiltWhenServiceThrowsJournalpostIkkeFerdigstiltException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new JournalpostIkkeFerdigstiltException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsJournalpostIkkeFerdigstiltWhenServiceThrowsFeilregistrertSaksrelasjonException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new FeilregistrertSaksrelasjonException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsJournalpostIkkeUnderArbeidWhenServiceThrowsIllegalJournalStatusException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new IllegalJournalStatusException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	@Test
	public void throwsEksterneVedleggIkkeTillattWhenServiceThrowsIllegalTilleggsopplysningerException() {
		KnyttDokumentTilJournalpostSomVedleggRequest requestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequest.class);
		KnyttDokumentTilJournalpostSomVedleggRequestTo mappedRequestMock = mock(KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		when(knyttDokumentTilJournalpostSomVedleggRequestMapperMock.map(any())).thenReturn(mappedRequestMock);

		doThrow(new IllegalTilleggsopplysningerException("Something failed"))
				.when(knyttDokumentTilJournalpostSomVedleggServiceMock)
				.knyttDokumentTilJournalpostSomVedlegg(mappedRequestMock);

		assertThrows(KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt.class,
				() -> provider.knyttDokumentTilJournalpostSomVedlegg(requestMock),
				"Something failed");
	}

	private AvbrytJournalpostRequest createAvbrytJournalpostRequest(long journalpostId, String endretAvNavn) {
		AvbrytJournalpostRequest avbrytJournalpostRequest = new AvbrytJournalpostRequest();
		avbrytJournalpostRequest.setEndretAvNavn(endretAvNavn);
		avbrytJournalpostRequest.setJournalpostId(journalpostId);
		return avbrytJournalpostRequest;
	}

	private AvbrytVedleggRequest createAvbrytVedlegRequest() {
		return new AvbrytVedleggRequest()
				.withJournalpostId(JOURNALPOST_ID)
				.withDokumentInfoId(DOCUMENT_INFO_ID)
				.withEndretAvNavn(ENDRET_AV_NAVN);
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