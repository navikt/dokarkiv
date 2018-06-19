package no.nav.dokarkiv.dokumentproduksjoninfo;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatus;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusRequestMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusRequestTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusResponseMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusResponseTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121.HentFerdigstilteDokumenterResponseMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121.HentFerdigstilteDokumenterResponseTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121.HentFerdigstilteDokumenterService;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122.HentJournalpostInfoService;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentFerdigstilteDokumenterUgyldingInput;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for DokumentproduksjonInfoProvider
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DokumentproduksjonInfoProviderTest {

	@Mock
	private HentJournalOgDokumentStatus hentJournalOgDokumentStatusMock;
	@Mock
	private HentFerdigstilteDokumenterService hentFerdigstilteDokumenterService;
	@Mock
	private HentJournalpostInfoService hentJournalpostInfoService;
	@Mock
	private HentJournalOgDokumentStatusRequestMapper hentJournalOgDokumentStatusRequestMapperMock;
	@Mock
	private HentJournalOgDokumentStatusResponseMapper hentJournalOgDokumentStatusResponseMapperMock;
	@Mock
	private HentFerdigstilteDokumenterResponseMapper hentFerdigstilteDokumenterServiceResponeMapper;
	
	@InjectMocks
	private DokumentproduksjonInfoProvider dokumentproduksjonInfoProvider;
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Test
	public void shouldDelegateToHentJournalOgDokumentStatus() throws Exception {
		HentJournalOgDokumentStatusRequest wsRequest = new HentJournalOgDokumentStatusRequest();
		HentJournalOgDokumentStatusResponse wsResponse = new HentJournalOgDokumentStatusResponse();
		HentJournalOgDokumentStatusRequestTo domainRequest = new HentJournalOgDokumentStatusRequestTo();
		HentJournalOgDokumentStatusResponseTo domainResponse = new HentJournalOgDokumentStatusResponseTo(null, null, null);
		when(hentJournalOgDokumentStatusRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		when(hentJournalOgDokumentStatusMock.hentJournalOgDokumentStatus(domainRequest)).thenReturn(domainResponse);
		when(hentJournalOgDokumentStatusResponseMapperMock.map(domainResponse)).thenReturn(wsResponse);
		
		HentJournalOgDokumentStatusResponse response = dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(wsRequest);
		
		assertThat(response, is(wsResponse));
	}
	
	@Test
	public void shouldThrowJournalpostIkkeFunnetWhenJournalpostNotFound() throws Exception {
		String exceptionMessage = "Test exception";
		setupExpectedExceptionProperties(HentJournalOgDokumentStatusJournalpostIkkeFunnet.class, exceptionMessage);
		
		HentJournalOgDokumentStatusRequest wsRequest = new HentJournalOgDokumentStatusRequest();
		HentJournalOgDokumentStatusRequestTo domainRequest = new HentJournalOgDokumentStatusRequestTo();
		when(hentJournalOgDokumentStatusRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		NoJournalpostFoundException domainException = new NoJournalpostFoundException(exceptionMessage, 1L);
		when(hentJournalOgDokumentStatusMock.hentJournalOgDokumentStatus(domainRequest)).thenThrow(
				domainException);
		
		dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(wsRequest);
	}
	
	@Test
	public void shouldThrowDokumentInfoIkkeFunnetWhenDokumentInfoNotFound() throws Exception {
		String exceptionMessage = "Test exception";
		setupExpectedExceptionProperties(HentJournalOgDokumentStatusDokumentInfoIkkeFunnet.class, exceptionMessage);
		
		HentJournalOgDokumentStatusRequest wsRequest = new HentJournalOgDokumentStatusRequest();
		HentJournalOgDokumentStatusRequestTo domainRequest = new HentJournalOgDokumentStatusRequestTo();
		when(hentJournalOgDokumentStatusRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		NoDokumentInfoFoundException domainException = new NoDokumentInfoFoundException(exceptionMessage, 1L);
		when(hentJournalOgDokumentStatusMock.hentJournalOgDokumentStatus(domainRequest)).thenThrow(
				domainException);
		
		dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(wsRequest);
	}
	
	@Test
	public void shouldDelegateToHentFerdigstilteDokumenter() throws Exception {
		HentFerdigstilteDokumenterRequest wsRequest = new HentFerdigstilteDokumenterRequest();
		wsRequest.setJournalpostId(1L);
		wsRequest.getDokumentInfoListe().add(2L);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(wsRequest);
		verify(hentFerdigstilteDokumenterServiceResponeMapper).map(anyListOf(HentFerdigstilteDokumenterResponseTo.class));
	}
	
	@Test
	public void shouldThrowException_HentFerdigstilteDokumenter_requestIsNull() throws Exception {
		expected.expect(HentFerdigstilteDokumenterUgyldingInput.class);
		expected.expectMessage("request is null");
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(null);
	}
	
	@Test
	public void shouldThrowException_HentFerdigstilteDokumenter_journalpostIsNull() throws Exception {
		expected.expect(HentFerdigstilteDokumenterUgyldingInput.class);
		expected.expectMessage("journalpostId is null");
		HentFerdigstilteDokumenterRequest wsRequest = new HentFerdigstilteDokumenterRequest();
		wsRequest.setJournalpostId(0);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(wsRequest);
	}
	
	@Test
	public void shouldThrowException_HentFerdigstilteDokumenter_dokumentInfosIsNull() throws Exception {
		expected.expect(HentFerdigstilteDokumenterUgyldingInput.class);
		expected.expectMessage("List with dokumentInfo is null or empty");
		
		HentFerdigstilteDokumenterRequest wsRequest = new HentFerdigstilteDokumenterRequest();
		wsRequest.setJournalpostId(1L);
		
		dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(wsRequest);
	}

	private void setupExpectedExceptionProperties(Class<? extends Exception> clazz, String exceptionMessage) {
		expected.expect(clazz);
		expected.expect(hasProperty("message", containsString(exceptionMessage)));
	}
}
