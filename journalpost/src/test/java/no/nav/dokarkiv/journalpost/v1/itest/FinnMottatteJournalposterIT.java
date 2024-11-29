package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import no.nav.dokarkiv.journalpost.v1.services.FinnMottatteJournalposterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.PEN;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.UFO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;


public class FinnMottatteJournalposterIT extends AbstractJournalpostIT {

	private static final String FINNMOTTATTEJOURNALPOSTER_PENSJON = "finnMottatteJournalposter/PEN/5";
	private static final int DEFAULT_DAGER_GAMLE = 5;

	@Autowired
	private FinnMottatteJournalposterService finnMottatteJournalposterService;

	@Test
	public void shouldHappyFinnMottatteJournalposter() {
		List<Journalpost> journalposts = List.of(
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now().minusWeeks(2)), JournalpostTypeCode.I, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now().minusWeeks(2)), JournalpostTypeCode.I, JournalStatusCode.M)
		);

		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId).toList();

		commitAndStartNewTransaction();

		List<Long> ubehandletJournalpostIds = finnMottatteJournalposterService
				.finnMottatteJournalposter()
				.getJournalposter()
				.stream()
				.map(UbehandletJournalpost::getJournalpostId).toList();

		assertTrue(ubehandletJournalpostIds.containsAll(journalpostIds));
	}

	@Test
	public void shouldOnlyGetUbehandledeJournalposts() {
		List<Date> journalDateRange = List.of(
				toDate(LocalDateTime.now().plusYears(1)),
				toDate(LocalDateTime.now().plusMonths(1)),
				toDate(LocalDateTime.now().plusWeeks(1)),
				toDate(LocalDateTime.now().plusDays(1)),
				toDate(LocalDateTime.now()),
				toDate(LocalDateTime.now().minusDays(1)),
				toDate(LocalDateTime.now().minusWeeks(1)),
				toDate(LocalDateTime.now().minusMonths(1)),
				toDate(LocalDateTime.now().minusYears(1))
		);

		ArrayList<Long> validJournalpostIds = new ArrayList<>();

		for (Date date : journalDateRange)
			for (JournalpostTypeCode journalpostTypeCode : JournalpostTypeCode.values())
				for (JournalStatusCode journalStatusCode : JournalStatusCode.values()) {
					Journalpost journalpost = saveJournalpost(TestDataUtils.createUbehandletJournalpost(date, journalpostTypeCode, journalStatusCode));
					if (verifyJournalpost(journalpost)) validJournalpostIds.add(journalpost.getJournalpostId());
				}

		commitAndStartNewTransaction();

		List<UbehandletJournalpost> ubehandletJournalposts = finnMottatteJournalposterService.finnMottatteJournalposter().getJournalposter();
		List<Long> retrievedIds = ubehandletJournalposts.stream().map(UbehandletJournalpost::getJournalpostId).toList();

		assertFalse(ubehandletJournalposts.isEmpty());
		assertEquals(retrievedIds.size(), validJournalpostIds.size());
		assertTrue(retrievedIds.containsAll(validJournalpostIds));

	}

	@Test
	public void shouldOnlyGetUbehandledeJournalpostsWithTemaUFOAndPEN() {
		List<Date> journalDateRange = List.of(
				toDate(LocalDateTime.now().plusYears(1)),
				toDate(LocalDateTime.now().plusMonths(1)),
				toDate(LocalDateTime.now().plusWeeks(1)),
				toDate(LocalDateTime.now().plusDays(1)),
				toDate(LocalDateTime.now()),
				toDate(LocalDateTime.now().minusDays(1)),
				toDate(LocalDateTime.now().minusWeeks(1)),
				toDate(LocalDateTime.now().minusMonths(1)),
				toDate(LocalDateTime.now().minusYears(1))
		);

		List<FagomradeCode> temakoder = List.of(FagomradeCode.AAP, UFO, FagomradeCode.BAR, PEN);

		ArrayList<Long> validJournalpostIds = new ArrayList<>();

		for (Date date : journalDateRange)
			for (JournalpostTypeCode journalpostTypeCode : JournalpostTypeCode.values())
				for (JournalStatusCode journalStatusCode : JournalStatusCode.values())
					for (FagomradeCode temakode : temakoder) {
						Journalpost journalpost = saveJournalpost(TestDataUtils.createUbehandletJournalpost(date, journalpostTypeCode, journalStatusCode, temakode));
						if (verifyJournalpostWithTema(journalpost))
							validJournalpostIds.add(journalpost.getJournalpostId());
					}

		commitAndStartNewTransaction();

		List<UbehandletJournalpost> ubehandletJournalposts = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(Set.of(UFO, PEN), DEFAULT_DAGER_GAMLE).getJournalposter();
		List<Long> retrievedIds = ubehandletJournalposts.stream().map(UbehandletJournalpost::getJournalpostId).collect(Collectors.toList());

		assertFalse(ubehandletJournalposts.isEmpty());
		assertEquals(retrievedIds.size(), validJournalpostIds.size());
		assertTrue(retrievedIds.containsAll(validJournalpostIds));

		ubehandletJournalposts = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(Set.of(UFO, PEN), DEFAULT_DAGER_GAMLE).getJournalposter();
		retrievedIds = ubehandletJournalposts.stream().map(UbehandletJournalpost::getJournalpostId).toList();

		assertFalse(ubehandletJournalposts.isEmpty());
		assertEquals(retrievedIds.size(), validJournalpostIds.size());
		assertTrue(retrievedIds.containsAll(validJournalpostIds));

	}

	@Test
	public void shouldFailFinnMottatteJournalposter() {
		List<Journalpost> journalposts = List.of(
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now()), JournalpostTypeCode.I, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now().minusWeeks(2)), JournalpostTypeCode.U, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now().minusWeeks(2)), JournalpostTypeCode.I, JournalStatusCode.U)
		);

		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId).toList();

		commitAndStartNewTransaction();

		List<Long> ubehandletJournalpostIds = finnMottatteJournalposterService
				.finnMottatteJournalposter()
				.getJournalposter()
				.stream()
				.map(UbehandletJournalpost::getJournalpostId).toList();

		assertFalse(journalpostIds.stream().anyMatch(ubehandletJournalpostIds::contains));
	}

	@Test
	public void returnsOKWithResponseJSONifValidRequest() {
		List<Journalpost> journalposts = List.of(
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now().minusWeeks(2)), JournalpostTypeCode.I, JournalStatusCode.MO),
				TestDataUtils.createUbehandletJournalpost(toDate(LocalDateTime.now().minusWeeks(2)), JournalpostTypeCode.I, JournalStatusCode.M)
		);

		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId).toList();

		commitAndStartNewTransaction();
		var requestEntity = new HttpEntity<>(null, createHeaders(SERVICE_USER_ID));

		ResponseEntity<FinnMottatteJournalposterResponse> response = restTemplate.exchange(apiInternalPath(FINNMOTTATTEJOURNALPOSTER_PENSJON), GET, requestEntity, FinnMottatteJournalposterResponse.class);

		HttpStatusCode status = response.getStatusCode();
		FinnMottatteJournalposterResponse body = response.getBody();

		assertEquals(HttpStatus.OK, status);
		assertNotNull(body);

		List<Long> ubehandletJournalpostIds = body.getJournalposter().stream().map(UbehandletJournalpost::getJournalpostId).toList();

		assertTrue(ubehandletJournalpostIds.containsAll(journalpostIds));
	}

	@Test
	public void returnsUnauthorizedWhenNoAuthorization() {
		var requestEntity = new HttpEntity<>(null, new HttpHeaders());

		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(FINNMOTTATTEJOURNALPOSTER_PENSJON), GET, requestEntity, String.class);

		HttpStatusCode status = response.getStatusCode();

		assertEquals(HttpStatus.UNAUTHORIZED, status);
	}


	private HttpHeaders createHeaders(String serviceUser) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + restStsToken(serviceUser));

		return headers;
	}

	private boolean verifyJournalpost(Journalpost journalpost) {
		Date weekAgo = toDate(LocalDateTime.now().minusWeeks(1));
		Date createdDate = journalpost.getChangeStamp().getCreatedDate();
		JournalStatusCode status = journalpost.getJournalstatus();

		if (createdDate.after(weekAgo)) return false;
		if (status != JournalStatusCode.M && status != JournalStatusCode.MO) return false;
		return journalpost.isInngaende();
	}

	private boolean verifyJournalpostWithTema(Journalpost journalpost) {
		Date weekAgo = toDate(LocalDateTime.now().minusWeeks(1));
		Date createdDate = journalpost.getChangeStamp().getCreatedDate();
		JournalStatusCode status = journalpost.getJournalstatus();
		FagomradeCode fagomrade = journalpost.getFagomrade();

		if (createdDate.after(weekAgo)) return false;
		if (status != JournalStatusCode.M && status != JournalStatusCode.MO) return false;
		if (!journalpost.isInngaende()) return false;
		return fagomrade == PEN || fagomrade == UFO;
	}

	private static Date toDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.of("Europe/Oslo")).toInstant());
	}
}