package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import no.nav.dokarkiv.journalpost.v1.services.FinnMottatteJournalposterService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class FinnMottatteJournalposterIT extends AbstractJournalpostIT {

	private static final String FINNMOTTATTEJOURNALPOSTER = "finnMottatteJournalposter";
	private static final String GYLDIG_CONSUMER = "srvdokarkivproxy";
	private static final String UGYLDIG_CONSUMER = "srvdokarkiv";
	private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
	private static final String FAGKODE_UFO = "UFO";
	private static final String FAGKODE_PEN = "PEN";

	private FinnMottatteJournalposterService finnMottatteJournalposterService;

	@Before
	public void setup(){
		finnMottatteJournalposterService = new FinnMottatteJournalposterService(joarkRepository);
	}

	@Test
	public void shouldHappyFinnMottatteJournalposter(){
		abacPermit();

		List<Journalpost> journalposts = List.of(
				TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(2).toDate(), JournalpostTypeCode.I, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(2).toDate(), JournalpostTypeCode.I, JournalStatusCode.M)
		);

		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId)
				.collect(Collectors.toList());

		reinitTransaction();

		List<Long> ubehandletJournalpostIds = finnMottatteJournalposterService
				.finnMottatteJournalposter()
				.getJournalposter()
				.stream()
				.map(UbehandletJournalpost::getJournalpostId)
				.collect(Collectors.toList());

		assertTrue(ubehandletJournalpostIds.containsAll(journalpostIds));
	}

	@Test
	public void shouldOnlyGetUbehandledeJournalposts(){
		List<Date> journalDateRange = List.of(
				DateTime.now().plusYears(1).toDate(),
				DateTime.now().plusMonths(1).toDate(),
				DateTime.now().plusWeeks(1).toDate(),
				DateTime.now().plusDays(1).toDate(),
				DateTime.now().toDate(),
				DateTime.now().minusDays(1).toDate(),
				DateTime.now().minusWeeks(1).toDate(),
				DateTime.now().minusMonths(1).toDate(),
				DateTime.now().minusYears(1).toDate()
		);

		ArrayList<Long> validJournalpostIds = new ArrayList<>();

		for (Date date : journalDateRange)
			for (JournalpostTypeCode journalpostTypeCode : JournalpostTypeCode.values())
				for (JournalStatusCode journalStatusCode : JournalStatusCode.values()){
					Journalpost journalpost = saveJournalpost(TestDataUtils.createUbehandletJournalpost(date, journalpostTypeCode, journalStatusCode));
					if(verifyJournalpost(journalpost)) validJournalpostIds.add(journalpost.getJournalpostId());
				}

		reinitTransaction();

		List<UbehandletJournalpost> ubehandletJournalposts = finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter();
		List<Long> retrievedIds = ubehandletJournalposts.stream().map(UbehandletJournalpost::getJournalpostId).collect(Collectors.toList());

		assertFalse(ubehandletJournalposts.isEmpty());
		assertEquals(retrievedIds.size(), validJournalpostIds.size());
		assertTrue(retrievedIds.containsAll(validJournalpostIds));

	}

	@Test
	public void shouldOnlyGetUbehandledeJournalpostsWithTemaUFOAndPEN() {
		List<Date> journalDateRange = List.of(
				DateTime.now().plusYears(1).toDate(),
				DateTime.now().plusMonths(1).toDate(),
				DateTime.now().plusWeeks(1).toDate(),
				DateTime.now().plusDays(1).toDate(),
				DateTime.now().toDate(),
				DateTime.now().minusDays(1).toDate(),
				DateTime.now().minusWeeks(1).toDate(),
				DateTime.now().minusMonths(1).toDate(),
				DateTime.now().minusYears(1).toDate()
		);

		List<FagomradeCode> temakoder = List.of(FagomradeCode.AAP, FagomradeCode.UFO, FagomradeCode.BAR, FagomradeCode.PEN);

		ArrayList<Long> validJournalpostIds = new ArrayList<>();

		for (Date date : journalDateRange)
			for (JournalpostTypeCode journalpostTypeCode : JournalpostTypeCode.values())
				for (JournalStatusCode journalStatusCode : JournalStatusCode.values())
					for (FagomradeCode temakode : temakoder) {
						Journalpost journalpost = saveJournalpost(TestDataUtils.createUbehandletJournalpost(date, journalpostTypeCode, journalStatusCode, temakode));
						if(verifyJournalpostWithTema(journalpost)) validJournalpostIds.add(journalpost.getJournalpostId());
					}

		reinitTransaction();

		List<UbehandletJournalpost> ubehandletJournalposts = finnMottatteJournalposterService.finnMottatteJournalposterMedTema(List.of(FAGKODE_UFO, FAGKODE_PEN)).getJournalposter();
		List<Long> retrievedIds = ubehandletJournalposts.stream().map(UbehandletJournalpost::getJournalpostId).collect(Collectors.toList());

		assertFalse(ubehandletJournalposts.isEmpty());
		assertEquals(retrievedIds.size(), validJournalpostIds.size());
		assertTrue(retrievedIds.containsAll(validJournalpostIds));

		ubehandletJournalposts = finnMottatteJournalposterService.finnMottatteJournalposterMedTema(List.of(FAGKODE_UFO, FAGKODE_PEN, "FinnesIkke")).getJournalposter();
		retrievedIds = ubehandletJournalposts.stream().map(UbehandletJournalpost::getJournalpostId).collect(Collectors.toList());

		assertFalse(ubehandletJournalposts.isEmpty());
		assertEquals(retrievedIds.size(), validJournalpostIds.size());
		assertTrue(retrievedIds.containsAll(validJournalpostIds));

	}

	@Test
	public void shouldFailFinnMottatteJournalposter(){
		abacPermit();

		List<Journalpost> journalposts = List.of(
				TestDataUtils.createUbehandletJournalpost(DateTime.now().toDate(), JournalpostTypeCode.I, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(2).toDate(), JournalpostTypeCode.U, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(2).toDate(), JournalpostTypeCode.I, JournalStatusCode.U)
		);

		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId)
				.collect(Collectors.toList());

		reinitTransaction();

		List<Long> ubehandletJournalpostIds = finnMottatteJournalposterService
				.finnMottatteJournalposter()
				.getJournalposter()
				.stream()
				.map(UbehandletJournalpost::getJournalpostId)
				.collect(Collectors.toList());

		assertFalse(journalpostIds.stream().anyMatch(ubehandletJournalpostIds::contains));
	}

	@Test
	public void returnsOKWithResponseJSONifValidRequest() throws IOException {
		abacPermit();

		List<Journalpost> journalposts = List.of(
				TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(2).toDate(), JournalpostTypeCode.I, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(DateTime.now().minusWeeks(2).toDate(), JournalpostTypeCode.I, JournalStatusCode.M)
		);

		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId)
				.collect(Collectors.toList());

		reinitTransaction();

		HttpEntity requestEntity = new HttpEntity<>(null, createHeaders(GYLDIG_CONSUMER));

		ResponseEntity<FinnMottatteJournalposterResponse> response = restTemplate.exchange(URL_JOURNALPOST_INTERN +FINNMOTTATTEJOURNALPOSTER, HttpMethod.GET, requestEntity, FinnMottatteJournalposterResponse.class);


		HttpStatus status = response.getStatusCode();
		FinnMottatteJournalposterResponse body = response.getBody();

		assertEquals(HttpStatus.OK, status);
		assertNotNull(body);

		List<Long> ubehandletJournalpostIds = body.getJournalposter().stream().map(UbehandletJournalpost::getJournalpostId).collect(Collectors.toList());

		assertTrue(ubehandletJournalpostIds.containsAll(journalpostIds));
	}

	@Test
	public void returnsBadRequestIfNoAuthorizationHeader() throws IOException {
		abacPermit();

		HttpEntity requestEntity = new HttpEntity<>(null, new HttpHeaders());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST_INTERN +FINNMOTTATTEJOURNALPOSTER, HttpMethod.GET, requestEntity, String.class);

		HttpStatus status = response.getStatusCode();

		assertEquals(HttpStatus.BAD_REQUEST, status);
	}

	@Test
	public void returnsForbiddenIfInvalidConsumer() throws IOException {
		abacPermit();

		HttpEntity requestEntity = new HttpEntity<>(null, createHeaders(UGYLDIG_CONSUMER));

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST_INTERN +FINNMOTTATTEJOURNALPOSTER, HttpMethod.GET, requestEntity, String.class);

		HttpStatus status = response.getStatusCode();

		assertEquals(HttpStatus.FORBIDDEN, status);
	}

	private HttpHeaders createHeaders(String consumer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Nav-Consumer-Id", NAV_CONSUMER_ID);
		String token = Base64Utils.encodeToString(
				(consumer + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		return headers;
	}

	private boolean verifyJournalpost(Journalpost journalpost) {
		Date weekAgo = DateTime.now().minusWeeks(1).toDate();
		Date createdDate = journalpost.getChangeStamp().getCreatedDate();
		JournalStatusCode status = journalpost.getJournalstatus();

		if( createdDate.after(weekAgo) ) return false;
		if( status != JournalStatusCode.M && status != JournalStatusCode.MO ) return false;
		if( !journalpost.isInngaende() ) return false;

		return true;
	}

	private boolean verifyJournalpostWithTema(Journalpost journalpost) {
		Date weekAgo = DateTime.now().minusWeeks(1).toDate();
		Date createdDate = journalpost.getChangeStamp().getCreatedDate();
		JournalStatusCode status = journalpost.getJournalstatus();
		FagomradeCode fagomrade = journalpost.getFagomrade();

		if( createdDate.after(weekAgo) ) return false;
		if( status != JournalStatusCode.M && status != JournalStatusCode.MO ) return false;
		if( !journalpost.isInngaende() ) return false;
		if ( fagomrade != FagomradeCode.PEN && fagomrade != FagomradeCode.UFO ) return false;

		return true;
	}
}