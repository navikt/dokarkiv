package no.nav.dokarkiv.journal.v3.tjoark058;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.journalpostliste.HentMinJPListeParameters;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.orm.hibernate5.HibernateTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Unit test for {@link DefaultHentKjerneJournalpostListeService}
 * @author Stig Strøm, Acando
 *
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class DefaultHentKjerneJournalpostListeServiceTest {
	

	private static final int LAST_PAGE_NR = 9;
	private static final FagsystemCode FAGSYSTEM = FagsystemCode.FS22;
	private static final long HUNDRED_JOURNALPOSTS = 100L;
	private static final JournalpostTypeCode JOURNALPOST_TYPE = JournalpostTypeCode.I;
	private static final int LIST_SIZE = 100;
	private static final Date DATE_TOM = new Date();
	private static final Date DATE_FOM = new Date(DATE_TOM.getTime() - 1);
	private static final List<FagomradeCode> FAGOMRAADE_LIST = Arrays.asList(FagomradeCode.AAP, FagomradeCode.FOR);
	private static final String SAK_ID = "sakID";
	private static final List<SakFagsystem> SAKFAGSYSTEM_LIST = Arrays.asList(new SakFagsystem(FAGSYSTEM, SAK_ID));
	@Mock
	private JoarkRepository joarkRepositoryMock;
	@Mock
	private HibernateTemplate hibernateTemplate;
	@Mock
	private Session sessionMock;
	@Mock
	private SessionFactory sessionFactoryMock;
	@Captor
	private ArgumentCaptor<HentMinJPListeParameters> captureParams;
	
	
	@InjectMocks
	private DefaultHentKjerneJournalpostListeService service;
	
	@Before
	public void setUp() throws Exception {
		when(hibernateTemplate.getSessionFactory()).thenReturn(sessionFactoryMock);
		when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);		
	}
	
	@Test
	public void shouldSearchAndReturnListWithJournalpost() throws Exception {
//		when(joarkRepositoryMock.findJournalpostListe(captureParams.capture())).thenReturn(searchResultWith100Journalposts());
//		when(joarkRepositoryMock.findTotalNumberOfJournalposts(any(HentMinJPListeParameters.class))).thenReturn(HUNDRED_JOURNALPOSTS);
		
		HentKjerneJournalpostListeRequestTo requestTo = HentKjerneJournalpostListeRequestTo.builder()
				.saksListe(SAKFAGSYSTEM_LIST)
				.journalFom(DATE_FOM)
				.journalTom(DATE_TOM)
				.journalpostType(JOURNALPOST_TYPE)
				.tema(FAGOMRAADE_LIST)
				.resultatSettStoerrelse(HUNDRED_JOURNALPOSTS)
				.resultatSettNr(0)				
				.build();
		
		HentKjerneJournalpostListeResponseTo responseTo = service.hentKjerneJournalpostListe(requestTo);
		
		assertThat(responseTo.getJournalpostListe(), hasSize(LIST_SIZE));
		assertThat(responseTo.isSisteIntervall(), is(true));	
		HentMinJPListeParameters minJPListeParameters = captureParams.getValue();
		assertThat(minJPListeParameters.getFagomraade(), is(FAGOMRAADE_LIST));
		assertThat(minJPListeParameters.getJournalFom(), is(DATE_FOM));
		assertThat(minJPListeParameters.getJournalTom(), is(DATE_TOM));
		assertThat(minJPListeParameters.getJournalpostTypeCode(), is(JOURNALPOST_TYPE));
		assertThat(minJPListeParameters.getMaxResults(), is(HUNDRED_JOURNALPOSTS));
		assertThat(minJPListeParameters.getPageNr(), is(0));
		assertThat(minJPListeParameters.getSaksListe(), hasSize(1));
		assertThat(minJPListeParameters.getSaksListe(), is(SAKFAGSYSTEM_LIST));
	}
		
	@Test
	public void emptyRequestShouldReturnEmptyResponse() throws Exception {
		HentKjerneJournalpostListeResponseTo responseTo = service.hentKjerneJournalpostListe(HentKjerneJournalpostListeRequestTo.builder().build());
		
		assertThat(responseTo.getJournalpostListe(), is(empty()));
		assertThat(responseTo.isSisteIntervall(), is(true));		
	}
	
	@Test
	public void shouldReturnEmptyResponse_searchReturnsNull() throws Exception {
//		when(joarkRepositoryMock.findJournalpostListe(captureParams.capture())).thenReturn(null);
		HentKjerneJournalpostListeResponseTo responseTo = service.hentKjerneJournalpostListe(HentKjerneJournalpostListeRequestTo.builder().build());
		
		assertThat(responseTo.getJournalpostListe(), is(nullValue()));
		assertThat(responseTo.isSisteIntervall(), is(true));		
	}	
	
	
	@Test
	public void shouldReturnFirst100JournalpostsAndNotIsSisteIntervall() throws Exception {
//		when(joarkRepositoryMock.findJournalpostListe(captureParams.capture())).thenReturn(searchResultWith100Journalposts());
//		when(joarkRepositoryMock.findTotalNumberOfJournalposts(any(HentMinJPListeParameters.class))).thenReturn(HUNDRED_JOURNALPOSTS + 1);
		HentKjerneJournalpostListeRequestTo requestTo = HentKjerneJournalpostListeRequestTo.builder()
				.saksListe(SAKFAGSYSTEM_LIST)
				.resultatSettStoerrelse(HUNDRED_JOURNALPOSTS)
				.build();
		HentKjerneJournalpostListeResponseTo responseTo = service.hentKjerneJournalpostListe(requestTo);
		
		assertThat(responseTo.getJournalpostListe(), hasSize(LIST_SIZE));
		assertThat(responseTo.isSisteIntervall(), is(false));		
	}
	
	@Test
	public void shouldReturnAllJournalpostsAndIsSisteIntervall() throws Exception {
//		when(joarkRepositoryMock.findJournalpostListe(captureParams.capture())).thenReturn(searchResultWith100Journalposts());
//		when(joarkRepositoryMock.findTotalNumberOfJournalposts(any(HentMinJPListeParameters.class))).thenReturn(HUNDRED_JOURNALPOSTS);
		HentKjerneJournalpostListeRequestTo requestTo = HentKjerneJournalpostListeRequestTo.builder()
				.saksListe(SAKFAGSYSTEM_LIST)
				.resultatSettStoerrelse(HUNDRED_JOURNALPOSTS)
				.resultatSettNr(0)
				.build();
		HentKjerneJournalpostListeResponseTo responseTo = service.hentKjerneJournalpostListe(requestTo);
		
		assertThat(responseTo.getJournalpostListe(), hasSize(LIST_SIZE));
		assertThat(responseTo.isSisteIntervall(), is(true));		
	}	
	
	@Test
	public void shouldReturnJournalpost91to100AndIsSisteIntervall() throws Exception {
//		when(joarkRepositoryMock.findJournalpostListe(captureParams.capture())).thenReturn(searchResultWith100Journalposts());
//		when(joarkRepositoryMock.findTotalNumberOfJournalposts(any(HentMinJPListeParameters.class))).thenReturn(HUNDRED_JOURNALPOSTS);
		HentKjerneJournalpostListeRequestTo requestTo = HentKjerneJournalpostListeRequestTo.builder()
				.saksListe(SAKFAGSYSTEM_LIST)
				.resultatSettStoerrelse(HUNDRED_JOURNALPOSTS/10)
				.resultatSettNr(LAST_PAGE_NR)
				.build();
		HentKjerneJournalpostListeResponseTo responseTo = service.hentKjerneJournalpostListe(requestTo);
		
		assertThat(responseTo.getJournalpostListe(), hasSize(LIST_SIZE));
		assertThat(responseTo.isSisteIntervall(), is(true));		
	}
	
	private List<Journalpost> searchResultWith100Journalposts() {
		List<Journalpost> list = new ArrayList<Journalpost>();
		for (int i = 0; i < LIST_SIZE; i++) {
			list.add(new Journalpost());
		}
		return list;
	}
}
