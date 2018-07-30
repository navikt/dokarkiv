package no.nav.dokarkiv.journal.v3;

import static no.nav.dokarkiv.journal.v3.JournalV3Provider.JOURNAL_V3_HENT_DOKUMENT;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.journal.v3.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.journal.v3.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentURLV3RequestMapper;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentUrlRequestTo;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentUrlResponseTo;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentUrlService;
import no.nav.dokarkiv.journal.v3.tjoark051.HentDokumentRequestTo;
import no.nav.dokarkiv.journal.v3.tjoark051.HentDokumentService;
import no.nav.dokarkiv.journal.v3.tjoark051.HentDokumentV3RequestMapper;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeRequestMapper;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeRequestTo;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeRequestValidator;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeResponseMapper;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeResponseTo;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeService;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.feil.DokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.feil.ForretningsmessigUnntak;
import no.nav.tjeneste.virksomhet.journal.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Unit tests for JournalV3Provider.
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalV3ProviderTest {
	
	private static final String JOURNALPOST_ID = "1";
	private static final String DOKUMENT_ID = "42";
	private static final byte[] FIL_INNHOLD = "fil".getBytes();
	private static final String FEIL_AARSAK = "feilAarsak";
	private static final String FEIL_KILDE = "feilKilde";
	private static final String EXCEPTION_MESSAGE = "Exception message";
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Mock
	private JournalV3FaultInfoPopulator faultInfoPopulatorMock;
	@Mock
	private HentKjerneJournalpostListeRequestValidator hentKjerneJournalpostListeRequestValidator;
	@Mock
	private HentKjerneJournalpostListeRequestMapper hentKjerneJournalpostListeRequestMapper;
	@Mock
	private HentKjerneJournalpostListeResponseMapper hentKjerneJournalpostListeResponseMapper;
	@Mock
	private HentKjerneJournalpostListeService hentKjerneJournalpostListeService;
	@Mock
	private HentDokumentV3RequestMapper hentDokumentRequestMapper;
	@Mock
	private HentDokumentService hentDokumentService;
	@Mock
	private AbacSecurityService abacSecurityService;
	@Mock
	private AbacContext abacContext;
	@Mock
	private HentDokumentURLV3RequestMapper hentDokumentURLV3RequestMapper;
	@Mock
	private HentDokumentUrlService hentDokumentUrlService;
	
	@InjectMocks
	private JournalV3Provider journalProvider;
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	private HentKjerneJournalpostListeRequest hentKjerneJournalpostListeRequest;
	private HentKjerneJournalpostListeRequestTo requestTo;
	private HentKjerneJournalpostListeResponseTo responseTo;
	private HentKjerneJournalpostListeResponse wsResponse;
	
	@Before
	public void setUp() {
		hentKjerneJournalpostListeRequest = new HentKjerneJournalpostListeRequest();
		requestTo = HentKjerneJournalpostListeRequestTo.builder().build();
		responseTo = HentKjerneJournalpostListeResponseTo.builder().build();
		wsResponse = new HentKjerneJournalpostListeResponse();
		when(abacContext.getRequest()).thenReturn(new XacmlRequest());
		
	}
	
	@Test
	public void hentKjerneJournalpostListeShouldDelegateToService() throws Exception {
		when(hentKjerneJournalpostListeRequestMapper.map(eq(hentKjerneJournalpostListeRequest), anyListOf(ArkivSak.class))).thenReturn(requestTo);
		when(hentKjerneJournalpostListeService.hentKjerneJournalpostListe(requestTo)).thenReturn(responseTo);
		when(hentKjerneJournalpostListeResponseMapper.map(responseTo)).thenReturn(wsResponse);
		
		assertThat(journalProvider.hentKjerneJournalpostListe(hentKjerneJournalpostListeRequest), is(wsResponse));
	}
	
	@Test
	public void hentKjerneJournalpostListeShouldThrowUgyldigInputException() throws Exception {
		doThrow(new IllegalArgumentException("hentKjerneJournalpostListeShouldThrowUgyldigInputException")).when(hentKjerneJournalpostListeRequestValidator)
				.validate(hentKjerneJournalpostListeRequest);
		
		thrown.expect(HentKjerneJournalpostListeUgyldigInput.class);
		thrown.expectMessage("hentKjerneJournalpostListeShouldThrowUgyldigInputException");
		journalProvider.hentKjerneJournalpostListe(hentKjerneJournalpostListeRequest);
	}
	
	@Test
	public void shouldFilterSaksIdsByAbacAccess() throws Exception {
		ArkivSak accessableArkivSak = new ArkivSak().withArkivSakId("1").withArkivSakSystem("FS22");
		ArkivSak notAccesableArkivSak = new ArkivSak().withArkivSakId("2").withArkivSakSystem("PEN");
		
		
		when(abacSecurityService.assertAccessToSak(any(XacmlRequest.class), eq("1"), eq(FagsystemCode.FS22))).thenReturn(Decision.PERMIT);
		when(abacSecurityService.assertAccessToSak(any(XacmlRequest.class), eq("2"), eq(FagsystemCode.PEN))).thenReturn(Decision.DENY);
		
		hentKjerneJournalpostListeRequest.withArkivSakListe(accessableArkivSak, notAccesableArkivSak);
		
		journalProvider.hentKjerneJournalpostListe(hentKjerneJournalpostListeRequest);
		
		List<ArkivSak> filteredList = getFilteredArkivSakListFromRequestMapperMock();
		assertThat(filteredList, contains(accessableArkivSak));
	}
	
	@Test
	public void shouldThrowHentKjerneJournalpostListeSikkerhetsbegrensningWhenAbacFiltersToEmptyList() throws Exception {
		ArkivSak accessableArkivSak = new ArkivSak().withArkivSakId("1").withArkivSakSystem("FS22");
		ArkivSak notAccesableArkivSak = new ArkivSak().withArkivSakId("2").withArkivSakSystem("PEN");
//		when(abacSecurityService.assertAccessToSak(eq("1"), eq(FagsystemCode.FS22))).thenReturn(Decision.DENY);
//		when(abacSecurityService.assertAccessToSak(eq("2"), eq(FagsystemCode.PEN))).thenReturn(Decision.DENY);
		
		hentKjerneJournalpostListeRequest.withArkivSakListe(accessableArkivSak, notAccesableArkivSak);
		
		expected.expect(HentKjerneJournalpostListeSikkerhetsbegrensning.class);
		journalProvider.hentKjerneJournalpostListe(hentKjerneJournalpostListeRequest);
	}
	
	@Test
	public void shouldDelegateToHentDokumentUrlService() throws Exception {
		String dokumentUrl = "url";
		HentDokumentURLRequest wsRequest = new HentDokumentURLRequest().withJournalpostId("dummyId")
				.withDokumentId("dummyDokId")
				.withVariantformat(new Variantformater().withValue("verdi").withKodeRef("dummyKodeRef"));
		HentDokumentUrlRequestTo hentDokumentUrlRequest = new HentDokumentUrlRequestTo(1L, 2L, VariantFormatCode.ARKIV);
		HentDokumentUrlResponseTo domainResponse = new HentDokumentUrlResponseTo(dokumentUrl);
		HentDokumentURLResponse wsResponse = new HentDokumentURLResponse();
		wsResponse.setDokumentURL(dokumentUrl);
		
		when(hentDokumentURLV3RequestMapper.map(wsRequest)).thenReturn(hentDokumentUrlRequest);
		when(hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest)).thenReturn(domainResponse);
		
		HentDokumentURLResponse response = journalProvider.hentDokumentURL(wsRequest);
		
		assertThat(response.getDokumentURL(), is(dokumentUrl));
	}
	
	@Test
	public void shouldThrowDokumentIkkeFunnetWhenhentDokumentUrlFails() throws Exception {
		HentDokumentURLRequest wsRequest = new HentDokumentURLRequest().withJournalpostId("dummyId")
				.withDokumentId("dummyDokId")
				.withVariantformat(new Variantformater().withValue("verdi").withKodeRef("dummyKodeRef"));
		HentDokumentUrlRequestTo hentDokumentUrlRequest = new HentDokumentUrlRequestTo(1L, 2L, VariantFormatCode.ARKIV);
		
		String exceptionMessage = "Test exception";
		setupExpectedExceptionProperties(HentDokumentURLDokumentIkkeFunnet.class, exceptionMessage);
		
		when(faultInfoPopulatorMock.populateFaultInfo((DokumentIkkeFunnet) any(), (Exception) any(), (String) any()))
				.thenReturn(createDokumentIkkeFunnet());
		when(hentDokumentURLV3RequestMapper.map(wsRequest)).thenReturn(hentDokumentUrlRequest);
		when(hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest)).thenThrow(
				new DocumentNotFoundException(exceptionMessage, null));
		
		journalProvider.hentDokumentURL(wsRequest);
	}
	
	
	@Test
	public void shouldDelegateToHentDokument() throws Exception {
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, VariantFormatCode.ARKIV.name());
		
		HentDokumentRequestTo domainRequest = createHentDokumentRequestTo();
		when(hentDokumentRequestMapper.map(wsRequest)).thenReturn(domainRequest);
		when(hentDokumentService.hentDokument(eq(domainRequest))).thenReturn(FIL_INNHOLD);
		
		HentDokumentResponse wsResponse = journalProvider.hentDokument(wsRequest);
		
		assertThat(wsResponse.getDokument(), is(FIL_INNHOLD));
	}
	
	@Test
	public void hentDokumentThrowsException_inputIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Input request is null");
		journalProvider.hentDokument(null);
	}
	
	@Test
	public void hentDokumentThrowsException_journalpostIsMissing() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("JournalpostId is null or empty");
		
		journalProvider.hentDokument(createHentDokumentRequest(null, DOKUMENT_ID, VariantFormatCode.ARKIV.name()));
	}
	
	@Test
	public void hentDokumentThrowsException_journalpostIdIsEmpty() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("JournalpostId is null or empty");
		
		journalProvider.hentDokument(createHentDokumentRequest("", DOKUMENT_ID, VariantFormatCode.ARKIV.name()));
	}
	
	@Test
	public void hentDokumentThrowsException_dokumentIdIsMissing() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("DokumentId is null or empty");
		
		journalProvider.hentDokument(createHentDokumentRequest(JOURNALPOST_ID, null, VariantFormatCode.ARKIV.name()));
	}
	
	@Test
	public void hentDokumentThrowsException_dokumentIdIsEmpty() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("DokumentId is null or empty");
		
		journalProvider.hentDokument(createHentDokumentRequest(JOURNALPOST_ID, "", VariantFormatCode.ARKIV.name()));
	}
	
	@Test
	public void hentDokumentThrowsException_VariantFormatIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("VariantFormat is null");
		
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, VariantFormatCode.ARKIV.name());
		wsRequest.setVariantformat(null);
		journalProvider.hentDokument(wsRequest);
	}
	
	@Test
	public void hentDokumentThrowsException_VariantFormatValueIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("VariantFormat.Value is null or empty");
		
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, null);
		journalProvider.hentDokument(wsRequest);
	}
	
	@Test
	public void hentDokumentThrowsException_VariantFormatValueIsEmpty() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("VariantFormat.Value is null or empty");
		
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, "");
		journalProvider.hentDokument(wsRequest);
	}
	
	@Test
	public void hentDokumentThrows_HentDokumentDokumentIkkeFunnet() throws Exception {
		expected.expect(HentDokumentDokumentIkkeFunnet.class);
		
		HentDokumentRequestTo domainRequest = createHentDokumentRequestTo();
		when(hentDokumentRequestMapper.map(any(HentDokumentRequest.class))).thenReturn(domainRequest);
		when(hentDokumentService.hentDokument(eq(domainRequest))).thenThrow(
				new DocumentNotFoundException(new NoJournalpostFoundException("not found", Long.valueOf(JOURNALPOST_ID))));
		
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, VariantFormatCode.ARKIV.name());
		journalProvider.hentDokument(wsRequest);
	}
	
	@Test
	public void shouldThrowSikkerhetsbegreningsFromHentDokument() throws Exception {
		expected.expect(HentDokumentSikkerhetsbegrensning.class);
		
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, VariantFormatCode.ARKIV.name());
		
		AuthorizationException authorizationException = new AuthorizationException("Access denied");
		doThrow(authorizationException)
				.when(abacSecurityService).assertAccessToJournalpost(eq(wsRequest.getJournalpostId()));
		
		journalProvider.hentDokument(wsRequest);
		verify(faultInfoPopulatorMock)
				.populateFaultInfo(any(Sikkerhetsbegrensning.class),
						eq(authorizationException),
						eq(JOURNAL_V3_HENT_DOKUMENT));
	}
	
	@Test
	public void hentDokumentThrowsException_variantFormatIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("VariantFormat.Value is null");
		
		HentDokumentRequest wsRequest = createHentDokumentRequest(JOURNALPOST_ID, DOKUMENT_ID, null);
		journalProvider.hentDokument(wsRequest);
	}
	
	private HentDokumentRequestTo createHentDokumentRequestTo() {
		return new HentDokumentRequestTo(Long.valueOf(JOURNALPOST_ID), Long.valueOf(DOKUMENT_ID), VariantFormatCode.ARKIV);
	}
	
	private HentDokumentRequest createHentDokumentRequest(String journalpostId, String dokumentId, String variantFormat) {
		HentDokumentRequest wsRequest = new HentDokumentRequest();
		wsRequest.setJournalpostId(journalpostId);
		wsRequest.setDokumentId(dokumentId);
		Variantformater variantFormater = new Variantformater();
		variantFormater.setValue(variantFormat);
		wsRequest.setVariantformat(variantFormater);
		return wsRequest;
	}
	
	private List<ArkivSak> getFilteredArkivSakListFromRequestMapperMock() {
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(hentKjerneJournalpostListeRequestMapper).map(any(HentKjerneJournalpostListeRequest.class), captor.capture());
		return captor.getValue();
	}
	
	private DokumentIkkeFunnet createDokumentIkkeFunnet() {
		DokumentIkkeFunnet dokumentIkkeFunnet = new DokumentIkkeFunnet();
		dokumentIkkeFunnet.setFeilaarsak(FEIL_AARSAK);
		dokumentIkkeFunnet.setFeilkilde(FEIL_KILDE);
		dokumentIkkeFunnet.setFeilmelding(EXCEPTION_MESSAGE);
		dokumentIkkeFunnet.setTidspunkt(getXmlTimestamp());
		return dokumentIkkeFunnet;
	}
	
	private Sikkerhetsbegrensning createSikkerhetsbegrensning() {
		Sikkerhetsbegrensning sikkerhetsbegrensning = new Sikkerhetsbegrensning();
		sikkerhetsbegrensning.setFeilaarsak(FEIL_AARSAK);
		sikkerhetsbegrensning.setFeilkilde(FEIL_KILDE);
		sikkerhetsbegrensning.setFeilmelding(EXCEPTION_MESSAGE);
		sikkerhetsbegrensning.setTidspunkt(getXmlTimestamp());
		return sikkerhetsbegrensning;
	}
	
	private void setupExpectedExceptionProperties(Class<? extends Exception> clazz, String exceptionMessage) {
		expected.expect(clazz);
		expected.expect(hasProperty("message", containsString(exceptionMessage)));
		expected.expect(hasProperty("faultInfo", instanceOf(ForretningsmessigUnntak.class)));
		expected.expect(hasProperty("faultInfo", hasProperty("feilaarsak", is(FEIL_AARSAK))));
		expected.expect(hasProperty("faultInfo", hasProperty("feilkilde", is(FEIL_KILDE))));
		expected.expect(hasProperty("faultInfo", hasProperty("feilmelding", is(EXCEPTION_MESSAGE))));
		expected.expect(hasProperty("faultInfo", hasProperty("tidspunkt", isA(getXmlTimestamp().getClass())))); //Timestamp hentes på et annet tidspunkt i exception-koden vi tester, enn her. Kan derfor ikke teste "tidspunkt" på verdien.
	}
	
	private XMLGregorianCalendar getXmlTimestamp() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(DateProvider.getToday());
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new ApplicationException("Unable to create XMLGregorianCalendar", e);
		}
	}
}