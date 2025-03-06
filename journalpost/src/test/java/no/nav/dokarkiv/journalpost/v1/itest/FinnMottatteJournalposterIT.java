package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.MottattJournalpost;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.MottattJournalpostBruker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.GEN;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.PEN;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BEHANDLINGSTEMA;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createUbehandletJournalpost;
import static no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterController.FINN_MOTTATTE_JOURNALPOSTER_ROLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class FinnMottatteJournalposterIT extends AbstractJournalpostIT {

	private static final int ANTALL_DAGER = 5;
	private static final String FINNMOTTATTEJOURNALPOSTER_PATH = "/finnMottatteJournalposter";
	private static final String DOKSIKKERHETSNETT = "test-miljo:teamdokumenthandtering:doksikkerhetsnett";
	private static final String APP_APPESEN = "test-miljo:annetteam:skummelapp";
	private static final Date OPPRETTET_DATO = Date.from(ZonedDateTime.now().minusDays(ANTALL_DAGER).minusMinutes(10).toInstant());
	private static final Date FOR_NY_DATO = Date.from(ZonedDateTime.now().minusDays(ANTALL_DAGER).plusMinutes(10).toInstant());

	private static final Map<String, List<String>> MOTTATTEJOURNALPOSTER_QUERY = Map.of("tema", List.of("PEN"), "antallDagerGamle", List.of("5"));

	@ParameterizedTest
	@CsvSource(value = {
			"UGYLDIG,Mottok ugyldig verdi for tema. UGYLDIG er ikke en gyldig temakode",
			",Mottok ugyldig verdi for tema. Tema var null eller tom"})
	public void shouldReturnBadRequestWhenUgyldigTema(String fagomraadeCode, String expectedExceptionMessage) {

		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(DOKSIKKERHETSNETT, FINN_MOTTATTE_JOURNALPOSTER_ROLE));

		ResponseEntity<String> response = restTemplate.exchange(createUri(fagomraadeCode, 5), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString(expectedExceptionMessage));
	}

	@ParameterizedTest
	@CsvSource(value = {
			"-1,Mottok ugyldig verdi for antallDagerGamle. AntallDagerGamle:-1 ender opp utenfor spennet 01.01.2020 -> dagens dato",
			"2000,Mottok ugyldig verdi for antallDagerGamle. AntallDagerGamle:2000 ender opp utenfor spennet 01.01.2020 -> dagens dato"})
	public void shouldReturnBadRequestWhenUgyldigTema(int antallDagerGamle, String expectedExceptionMessage) {

		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(DOKSIKKERHETSNETT, FINN_MOTTATTE_JOURNALPOSTER_ROLE));

		ResponseEntity<String> response = restTemplate.exchange(createUri(PEN.name(), antallDagerGamle), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString(expectedExceptionMessage));
	}

	@ParameterizedTest
	@ValueSource(strings = {" ", "BAD_ROLE"})
	public void shouldReturnUnauthorizedWhenWrongRole(String role) {
		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(DOKSIKKERHETSNETT, role));

		ResponseEntity<String> response = restTemplate.exchange(apiPath(FINNMOTTATTEJOURNALPOSTER_PATH), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(UNAUTHORIZED));
	}

	@Test
	public void shouldReturnBadRequestForMissingQueryParams() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(DOKSIKKERHETSNETT, FINN_MOTTATTE_JOURNALPOSTER_ROLE));

		ResponseEntity<String> response = restTemplate.exchange(apiPath(FINNMOTTATTEJOURNALPOSTER_PATH), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
	}

	@Test
	public void shouldNotReturnAnyJournalposts() {
		List<Journalpost> journalposts = List.of(
				//Skal ikke returnere Utgående journalpost
				createUbehandletJournalpost(OPPRETTET_DATO, U, MO),
				//Skal ikke returnere notat
				createUbehandletJournalpost(OPPRETTET_DATO, N, M),
				//Skal ikke returnere tema GEN når det blir spurt på PEN
				createUbehandletJournalpost(OPPRETTET_DATO, I, M, GEN),
				//Skal ikke returnere en journalpost som ble opprettet for 5 dager siden når det blir spurt om eldre enn 5 dager
				createUbehandletJournalpost(FOR_NY_DATO, I, M, PEN)
		);
		saveJournalposts(journalposts);

		Set<MottattJournalpost> mottatteJournalposterResponse = doKallFinnMottatteJournalposterAndAssertOK(DOKSIKKERHETSNETT);
		assertThat(mottatteJournalposterResponse.size(), is(0));
	}

	@Test
	public void happyFinnMottatteJournalposter() {
		List<Journalpost> journalposts = List.of(
				createUbehandletJournalpost(OPPRETTET_DATO, I, MO),
				createUbehandletJournalpost(OPPRETTET_DATO, I, M)
		);
		List<Long> journalpostIds = saveJournalposts(journalposts);

		Set<MottattJournalpost> fagsystemJournalposter = doKallFinnMottatteJournalposterAndAssertOK(APP_APPESEN);
		assertThat(fagsystemJournalposter.size(), is(journalpostIds.size()));
		assertTrue(fagsystemJournalposter.stream().map(MottattJournalpost::getJournalpostId).toList().containsAll(journalpostIds));
		fagsystemJournalposter.forEach(jp -> {
			validateUbehandletJournalpost(jp);
			//Bruker returneres bare for doksikkerhetsnett
			assertThat(jp.getBruker(), is(nullValue()));
		});

		Set<MottattJournalpost> doksikkerhetsnettJournalposter = doKallFinnMottatteJournalposterAndAssertOK(DOKSIKKERHETSNETT);
		assertThat(doksikkerhetsnettJournalposter.size(), is(journalpostIds.size()));
		assertTrue(doksikkerhetsnettJournalposter.stream().map(MottattJournalpost::getJournalpostId).toList().containsAll(journalpostIds));
		doksikkerhetsnettJournalposter.forEach(jp -> {
			validateUbehandletJournalpost(jp);
			validateBruker(jp.getBruker());
		});
	}

	@Test
	public void shouldNotReturnBrukerForFagsystem() {
		List<Journalpost> journalposts = List.of(
				createUbehandletJournalpost(OPPRETTET_DATO, I, MO),
				createUbehandletJournalpost(OPPRETTET_DATO, I, M)
		);
		List<Long> journalpostIds = saveJournalposts(journalposts);

		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(APP_APPESEN, FINN_MOTTATTE_JOURNALPOSTER_ROLE));

		ResponseEntity<String> response = restTemplate.exchange(apiPath(MOTTATTEJOURNALPOSTER_QUERY, FINNMOTTATTEJOURNALPOSTER_PATH), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		assertThat(response.getBody(), not(containsString("bruker")));
		journalpostIds.forEach(jpId -> assertThat(response.getBody(), containsString("" + jpId)));
	}

	private URI createUri(String fagomraadeCode, Integer antallDagerGamle) {
		return UriComponentsBuilder.fromPath(apiMottatteJournalposterfoPath())
				.queryParam("tema", fagomraadeCode)
				.queryParam("antallDagerGamle", antallDagerGamle).build().toUri();
	}

	private List<Long> saveJournalposts(List<Journalpost> journalposts) {
		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId).toList();
		commitAndStartNewTransaction();
		return journalpostIds;
	}

	private void validateBruker(MottattJournalpostBruker mottattJournalpostBruker) {
		assertThat(mottattJournalpostBruker.getType(), is(PERSON.name()));
		assertThat(mottattJournalpostBruker.getId(), is(BRUKER_ID));
	}

	private void validateUbehandletJournalpost(MottattJournalpost jp) {
		assertThat(jp.getBehandlingstema(), is(BEHANDLINGSTEMA));
		assertThat(jp.getTema(), is(PEN.name()));
		assertThat(jp.getJournalStatus(), anyOf(is(MO.name()), is(M.name())));
		assertThat(jp.getMottaksKanal(), is(NAV_NO.name()));
		assertThat(jp.getJournalforendeEnhet(), is(JOURNALFOERENDE_ENHET));
		assertThat(jp.getDatoOpprettet(), is(OPPRETTET_DATO));
	}

	private Set<MottattJournalpost> doKallFinnMottatteJournalposterAndAssertOK(String app) {
		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(app, FINN_MOTTATTE_JOURNALPOSTER_ROLE));

		ResponseEntity<FinnMottatteJournalposterResponse> response = restTemplate.exchange(apiPath(MOTTATTEJOURNALPOSTER_QUERY, FINNMOTTATTEJOURNALPOSTER_PATH), GET, requestEntity, FinnMottatteJournalposterResponse.class);
		assertThat(response.getStatusCode(), is(OK));
		return response.getBody().getJournalposter();
	}
}