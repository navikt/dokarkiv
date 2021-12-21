package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseResponse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.ForretningsmessigUnntak;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerutgaaendehenvendelse.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.FerdigstillDokumentopplastingRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test class for BehandleJournalProvider.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class BehandleJournalV3ProviderTest {
	private static final String SPORING_FORNAVN = "Sigrid";
	private static final String SPORING_ETTERNAVN = "Saksbehandler";
	private static final String SPORING_APPLIKASJONS_ID = "JOARK";
	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENT_ID = 1L;
	private static final String FEIL_AARSAK = "feilAarsak";
	private static final String FEIL_KILDE = "feilKilde";
	private static final String EXCEPTION_MESSAGE = "Exception message";

	@Mock
	private BehandleJournalV3Pep behandleJournalV3PepMock;
	@Mock
	private BehandleJournalV3ServiceBi behandleJournalServiceMock;
	@Mock
	private BehandleJournalV3FaultInfoPopulator behandleJournalV3FaultInfoPopulatorMock;
	@Mock
	private ArkiverUstrukturertKravV3RequestMapper arkiverUstrukturertKravRequestMapperMock;
	@Mock
	private ArkiverUstrukturertKravV3ResponseMapper arkiverUstrukturertKravResponseMapperMock;
	@Mock
	private LagreVedleggPaaJournalpostV3RequestMapper lagreVedleggPaaJournalpostRequestMapperMock;
	@Mock
	private LagreVedleggPaaJournalpostV3ResponseMapper lagreVedleggPaaJournalpostResponseMapperMock;
	@Mock
	private JournalfoerInngaaendeHenvendelseV3RequestMapper journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapperMock;
	@Mock
	private JournalfoerInngaaendeHenvendelseV3ResponseMapper journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapperMock;
	@Mock
	private FerdigstillDokumentopplastingV3RequestMapper ferdigstillDokumentopplastingRequestMapper;
	@Mock
	private JournalfoerUtgaaendeHenvendelseV3RequestMapper JournalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapperMock;
	@Mock
	private JournalfoerUtgaaendeHenvendelseV3ResponseMapper journalfoerUtgaaendeHenvendelseResponseMapperMock;
	@Mock
	private JournalfoerNotatHenvendelseV3RequestMapper journalfoerNotatHenvendelseRequestMapperMock;
	@Mock
	private JournalfoerNotatHenvendelseV3ResponseMapper journalfoerNotatHenvendelseResponseMapperMock;

	@InjectMocks
	private BehandleJournalV3Provider behandleJournalV3Provider = new BehandleJournalV3Provider();
	@Rule
	public ExpectedException expected = ExpectedException.none();
	private JournalpostIkkeFunnet journalpostIkkeFunnet;

	@Before
	public void setUp() {
		DateProvider.configure(true, DateProvider.getDate(new Date()));
		journalpostIkkeFunnet = createJournalpostIkkeFunnet();
	}

	@After
	public void tearDown() {
		DateProvider.configure(false, null);
	}

	@Test
	public void shouldStoreJournalpostAndReturnJournalpostIdWhenArkiverUstrukturertKravIsCalled() {
		ArkiverUstrukturertKravRequest wsRequest = new ArkiverUstrukturertKravRequest();
		no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravResponse domainResponse = new no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravResponse(
				JOURNALPOST_ID, DOKUMENT_ID);
		no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravRequest(
				new no.nav.dokarkiv.core.domain.entities.Journalpost());
		ArkiverUstrukturertKravResponse wsResponse = new ArkiverUstrukturertKravResponse();
		wsResponse.setJournalpostId(JOURNALPOST_ID.toString());
		wsResponse.setDokumentId(DOKUMENT_ID.toString());

		when(arkiverUstrukturertKravRequestMapperMock.map(eq(wsRequest))).thenReturn(domainRequest);
		when(behandleJournalServiceMock.arkiverUstrukturertKrav(domainRequest)).thenReturn(domainResponse);
		when(arkiverUstrukturertKravResponseMapperMock.map(eq(domainResponse))).thenReturn(wsResponse);

		ArkiverUstrukturertKravResponse response = behandleJournalV3Provider.arkiverUstrukturertKrav(wsRequest);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID.toString()));
		assertThat(response.getDokumentId(), is(DOKUMENT_ID.toString()));
	}

	@Test
	public void shouldAddVedleggToJournalpostAndReturnDokumentIdWhenLagreVedleggPaaJournalpostIsCalled()
			throws Exception {
		LagreVedleggPaaJournalpostRequest wsRequest = new LagreVedleggPaaJournalpostRequest();
		LagreVedleggPaaJournalpostResponse wsResponse = new LagreVedleggPaaJournalpostResponse();
		wsResponse.setDokumentId(DOKUMENT_ID.toString());
		no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostResponse domainResponse = new no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostResponse(
				1L);
		no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequest(
				1L, new DokumentInfo(), createSporingsMetaData());

		when(lagreVedleggPaaJournalpostRequestMapperMock.map(eq(wsRequest))).thenReturn(domainRequest);
		when(behandleJournalServiceMock.lagreVedleggPaaJournalpost(domainRequest)).thenReturn(domainResponse);
		when(lagreVedleggPaaJournalpostResponseMapperMock.map(eq(domainResponse))).thenReturn(wsResponse);

		LagreVedleggPaaJournalpostResponse response = behandleJournalV3Provider.lagreVedleggPaaJournalpost(wsRequest);

		assertThat(response.getDokumentId(), is(DOKUMENT_ID.toString()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotAddVedleggToJournalpostAndThrowCheckedExceptionWhenLagreVedleggPaaJournalpostIsCalled()
			throws Exception {
		assertCheckedExceptionProperties(LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet.class);

		LagreVedleggPaaJournalpostRequest wsRequest = new LagreVedleggPaaJournalpostRequest();
		LagreVedleggPaaJournalpostResponse wsResponse = new LagreVedleggPaaJournalpostResponse();
		wsResponse.setDokumentId(DOKUMENT_ID.toString());
		no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequest(
				1L, new DokumentInfo(), createSporingsMetaData());

		when(lagreVedleggPaaJournalpostRequestMapperMock.map(eq(wsRequest))).thenReturn(domainRequest);
		when(behandleJournalServiceMock.lagreVedleggPaaJournalpost(domainRequest)).thenThrow(
				NoJournalpostFoundException.class);
		when(behandleJournalV3FaultInfoPopulatorMock.populateFaultInfo((JournalpostIkkeFunnet) any(),
				any(), any())).thenReturn(journalpostIkkeFunnet);

		behandleJournalV3Provider.lagreVedleggPaaJournalpost(wsRequest);
	}

	@Test
	public void shouldOppretteJournalpostAndReturnJournalpostIdWhenOpprettMidlertidigInngaaendeJournalpostIsCalled() {
		JournalfoerInngaaendeHenvendelseRequest wsRequest = new JournalfoerInngaaendeHenvendelseRequest();
		no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseResponse domainResponse = new no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseResponse(
				JOURNALPOST_ID);
		no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseRequest(
				new no.nav.dokarkiv.core.domain.entities.Journalpost());
		JournalfoerInngaaendeHenvendelseResponse wsResponse = new JournalfoerInngaaendeHenvendelseResponse();
		wsResponse.setJournalpostId(JOURNALPOST_ID.toString());

		when(journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapperMock.map(eq(wsRequest))).thenReturn(
				domainRequest);
		when(behandleJournalServiceMock.journalfoerInngaaendeHenvendelse(domainRequest)).thenReturn(
				domainResponse);
		when(journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapperMock.map(eq(domainResponse))).thenReturn(
				wsResponse);

		JournalfoerInngaaendeHenvendelseResponse response = behandleJournalV3Provider
				.journalfoerInngaaendeHenvendelse(wsRequest);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}

	@Test
	public void shouldDelegateToFerdigstillDokumentopplastingService() throws Exception {
		FerdigstillDokumentopplastingRequest wsRequest = new FerdigstillDokumentopplastingRequest();
		wsRequest.setJournalpostId(String.valueOf(JOURNALPOST_ID));
		no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequest(
				JOURNALPOST_ID, createSporingsMetaData());

		when(ferdigstillDokumentopplastingRequestMapper.map(wsRequest)).thenReturn(domainRequest);

		behandleJournalV3Provider.ferdigstillDokumentopplasting(wsRequest);

		verify(behandleJournalServiceMock).ferdigstillDokumentopplasting(domainRequest);
	}

	@Test
	public void shouldDelegateToFerdigstillDokumentopplastingServiceAndThrowCheckedExceptionWhenJournalpostIsNotFound()
			throws Exception {
		assertCheckedExceptionProperties(FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet.class);

		FerdigstillDokumentopplastingRequest wsRequest = new FerdigstillDokumentopplastingRequest();
		wsRequest.setJournalpostId(String.valueOf(JOURNALPOST_ID));
		no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequest(
				JOURNALPOST_ID, createSporingsMetaData());

		when(ferdigstillDokumentopplastingRequestMapper.map(wsRequest)).thenReturn(domainRequest);
		doThrow(NoJournalpostFoundException.class).when(behandleJournalServiceMock).ferdigstillDokumentopplasting(
				domainRequest);
		when(behandleJournalV3FaultInfoPopulatorMock.populateFaultInfo((JournalpostIkkeFunnet) any(),
						any(), any())).thenReturn(journalpostIkkeFunnet);

		behandleJournalV3Provider.ferdigstillDokumentopplasting(wsRequest);
	}

	@Test
	public void shouldDelegateTojournalfoerutgaaendehenvendelseServiceAndReturnResponse() {
		JournalfoerUtgaaendeHenvendelseRequest wsRequest = new JournalfoerUtgaaendeHenvendelseRequest();
		wsRequest.setJournalpost(new Journalpost());
		JournalfoerUtgaaendeHenvendelseResponse wsResponse = new JournalfoerUtgaaendeHenvendelseResponse();
		wsResponse.setJournalpostId(String.valueOf(JOURNALPOST_ID));
		no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseRequest domainRequest = new no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseRequest(
				new no.nav.dokarkiv.core.domain.entities.Journalpost());
		no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseResponse domainResponse = new no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseResponse(
				JOURNALPOST_ID);

		when(JournalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		when(journalfoerUtgaaendeHenvendelseResponseMapperMock.map(domainResponse)).thenReturn(
				wsResponse);
		when(behandleJournalServiceMock.journalfoerUtgaaendeHenvendelse(domainRequest)).thenReturn(
				domainResponse);

		JournalfoerUtgaaendeHenvendelseResponse response = behandleJournalV3Provider
				.journalfoerUtgaaendeHenvendelse(wsRequest);

		verify(behandleJournalServiceMock).journalfoerUtgaaendeHenvendelse(domainRequest);
		assertThat(response.getJournalpostId(), is(String.valueOf(JOURNALPOST_ID)));
	}

	@Test
	public void shouldDelegateToJournalfoerNotatHenvendelseServiceAndReturnResponse() throws Exception {
		JournalfoerNotatRequest wsRequest = new JournalfoerNotatRequest();
		wsRequest
				.setJournalpost(new no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoernotat.Journalpost());
		JournalfoerNotatResponse wsResponse = new JournalfoerNotatResponse();
		wsResponse.setJournalpostId(String.valueOf(JOURNALPOST_ID));
		JournalfoerNotatHenvendelseRequest domainRequest = new JournalfoerNotatHenvendelseRequest(
				new no.nav.dokarkiv.core.domain.entities.Journalpost());
		JournalfoerNotatHenvendelseResponse domainResponse = new JournalfoerNotatHenvendelseResponse(
				JOURNALPOST_ID);

		when(journalfoerNotatHenvendelseRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		when(journalfoerNotatHenvendelseResponseMapperMock.map(domainResponse)).thenReturn(wsResponse);
		when(behandleJournalServiceMock.journalfoerNotatHenvendelse(domainRequest)).thenReturn(
				domainResponse);

		JournalfoerNotatResponse response = behandleJournalV3Provider.journalfoerNotat(wsRequest);

		verify(behandleJournalServiceMock).journalfoerNotatHenvendelse(domainRequest);
		assertThat(response.getJournalpostId(), is(String.valueOf(JOURNALPOST_ID)));
	}

	private SporingsMetaData createSporingsMetaData() {
		return new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN, SPORING_APPLIKASJONS_ID);
	}

	private void assertCheckedExceptionProperties(Class<? extends Exception> clazz) {
		expected.expect(clazz);
		expected.expect(hasProperty("faultInfo", instanceOf(ForretningsmessigUnntak.class)));
		expected.expect(hasProperty("faultInfo", hasProperty("feilaarsak", is(FEIL_AARSAK))));
		expected.expect(hasProperty("faultInfo", hasProperty("feilkilde", is(FEIL_KILDE))));
		expected.expect(hasProperty("faultInfo", hasProperty("feilmelding", is(EXCEPTION_MESSAGE))));
		expected.expect(hasProperty("faultInfo", hasProperty("tidspunkt", is(getXmlTimestamp()))));
	}

	private JournalpostIkkeFunnet createJournalpostIkkeFunnet() {
		JournalpostIkkeFunnet journalpostIkkeFunnet = new JournalpostIkkeFunnet();
		journalpostIkkeFunnet.setFeilaarsak(FEIL_AARSAK);
		journalpostIkkeFunnet.setFeilkilde(FEIL_KILDE);
		journalpostIkkeFunnet.setFeilmelding(EXCEPTION_MESSAGE);
		journalpostIkkeFunnet.setTidspunkt(getXmlTimestamp());
		return journalpostIkkeFunnet;
	}

	private XMLGregorianCalendar getXmlTimestamp() {
		GregorianCalendar calendar = new GregorianCalendar();
		// Setting the date explicitly to make it testable
		calendar.setTime(DateProvider.getToday());
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new ApplicationException("Unable to create XMLGregorianCalendar", e);
		}
	}
}
