package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletBruker;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.UbehandletJournalpost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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

public class FinnMottatteJournalposterIT extends AbstractJournalpostIT {

	private static final int ANTALL_DAGER = 5;
	private static final String FINNMOTTATTEJOURNALPOSTER_BASE_PATH = "finnMottatteJournalposter/";
	private static final String FINNMOTTATTEJOURNALPOSTER_PENSJON = FINNMOTTATTEJOURNALPOSTER_BASE_PATH + "PEN/" + ANTALL_DAGER;
	private static final String DOKSIKKERHETSNETT = "test-miljo:teamdokumenthandtering:doksikkerhetsnett";
	private static final String APP_APPESEN = "test-miljo:annetteam:skummelapp";
	private static final Date OPPRETTET_DATO = Date.from(LocalDate.now().minusDays(ANTALL_DAGER + 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	private static final Date FOR_NY_DATO = Date.from(LocalDate.now().minusDays(ANTALL_DAGER).atStartOfDay(ZoneId.systemDefault()).toInstant());

	@ParameterizedTest
	@CsvSource(value = {
			"UGYLDIG,Mottok ugyldig verd for tema. UGYLDIG er ikke en gyldig temakode",
			",Mottok ugyldig verd for tema. null er ikke en gyldig temakode"})
	public void shouldReturnBadRequestWhenUgyldigTema(String fagomraadeCode, String expectedExceptionMessage) {

		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(DOKSIKKERHETSNETT, SERVICE_USER_ID));

		ResponseEntity<String> response = restTemplate.exchange(apiPath(FINNMOTTATTEJOURNALPOSTER_BASE_PATH + fagomraadeCode + "/" + ANTALL_DAGER), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString(expectedExceptionMessage));

	}

	@Test
	public void shouldReturnBadRequestForInvalidAntallDager(){
		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(DOKSIKKERHETSNETT, SERVICE_USER_ID));

		ResponseEntity<String> response = restTemplate.exchange(apiPath(FINNMOTTATTEJOURNALPOSTER_BASE_PATH + "PEN/TEST"), GET, requestEntity, String.class);
		//Automatisk bad request fra Spring da den forventer int i pathen
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
				createUbehandletJournalpost(FOR_NY_DATO, I, M, GEN)
		);
		saveJournalposts(journalposts);

		FinnMottatteJournalposterResponse ubehandledeJournalposterResponse = doKallFinnMottatteJournalposterAndAssertOK(FINNMOTTATTEJOURNALPOSTER_PENSJON, DOKSIKKERHETSNETT);
		assertThat(ubehandledeJournalposterResponse.getJournalposter().size(), is(0));
	}

	@Test
	public void happyFinnMottatteJournalposter() {
		List<Journalpost> journalposts = List.of(
				createUbehandletJournalpost(OPPRETTET_DATO, I, MO),
				createUbehandletJournalpost(OPPRETTET_DATO, I, M)
		);
		List<Long> journalpostIds = saveJournalposts(journalposts);

		FinnMottatteJournalposterResponse doksikkerhetsnettResponse = doKallFinnMottatteJournalposterAndAssertOK(FINNMOTTATTEJOURNALPOSTER_PENSJON, DOKSIKKERHETSNETT);
		assertThat(doksikkerhetsnettResponse.getJournalposter().size(), is(journalpostIds.size()));
		assertTrue(doksikkerhetsnettResponse.getJournalposter().stream().map(UbehandletJournalpost::getJournalpostId).toList().containsAll(journalpostIds));
		doksikkerhetsnettResponse.getJournalposter().forEach(jp -> {
			validateUbehandletJournalpost(jp);
			//Bruker returneres bare for doksikkerhetsnett
			validateBruker(jp.getBruker());
		});

		FinnMottatteJournalposterResponse fagsystemResponse = doKallFinnMottatteJournalposterAndAssertOK(FINNMOTTATTEJOURNALPOSTER_PENSJON, APP_APPESEN);
		assertThat(fagsystemResponse.getJournalposter().size(), is(journalpostIds.size()));
		assertTrue(fagsystemResponse.getJournalposter().stream().map(UbehandletJournalpost::getJournalpostId).toList().containsAll(journalpostIds));
		fagsystemResponse.getJournalposter().forEach(jp -> {
			validateUbehandletJournalpost(jp);
			//Bruker returneres bare for doksikkerhetsnett
			assertThat(jp.getBruker(), is(nullValue()));
		});
	}

	@Test
	public void shouldNotReturnBrukerForFagsystem() {
		List<Journalpost> journalposts = List.of(
				createUbehandletJournalpost(OPPRETTET_DATO, I, MO),
				createUbehandletJournalpost(OPPRETTET_DATO, I, M)
		);
		List<Long> journalpostIds = saveJournalposts(journalposts);

		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(APP_APPESEN, SERVICE_USER_ID));

		ResponseEntity<String> response = restTemplate.exchange(apiPath(FINNMOTTATTEJOURNALPOSTER_PENSJON), GET, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		assertThat(response.getBody(), not(containsString("bruker")));
		journalpostIds.forEach(jpId -> assertThat(response.getBody(), containsString("" + jpId)));
	}

	private List<Long> saveJournalposts(List<Journalpost> journalposts){
		List<Long> journalpostIds = journalposts.stream()
				.map(this::saveJournalpost)
				.map(Journalpost::getJournalpostId).toList();
		commitAndStartNewTransaction();
		return journalpostIds;
	}

	private void validateBruker(UbehandletBruker ubehandletBruker) {
		assertThat(ubehandletBruker.getType(), is(PERSON));
		assertThat(ubehandletBruker.getId(), is(BRUKER_ID));
	}

	private void validateUbehandletJournalpost(UbehandletJournalpost jp) {
		assertThat(jp.getBehandlingstema(), is(BEHANDLINGSTEMA));
		assertThat(jp.getTema(), is(PEN));
		assertThat(jp.getJournalStatus(), anyOf(is(MO), is(M)));
		assertThat(jp.getMottaksKanal(), is(NAV_NO));
		assertThat(jp.getJournalforendeEnhet(), is(JOURNALFOERENDE_ENHET));
		assertThat(jp.getDatoOpprettet(), is(OPPRETTET_DATO));
	}

	private FinnMottatteJournalposterResponse doKallFinnMottatteJournalposterAndAssertOK(String url, String app) {
		var requestEntity = new HttpEntity<>(null, createHeadersWithServiceUserTokenAndRolesClaim(app, SERVICE_USER_ID));

		ResponseEntity<FinnMottatteJournalposterResponse> response = restTemplate.exchange(apiPath(url), GET, requestEntity, FinnMottatteJournalposterResponse.class);
		assertThat(response.getStatusCode(), is(OK));
		return response.getBody();
	}
}