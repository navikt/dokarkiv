package no.nav.dokarkiv.journalpost.v1.api.avsluttSak;

import no.nav.dokarkiv.core.domain.codes.AvleveringStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.itest.AbstractJournalpostIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.Instant.now;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.KassasjonStatusCode.KLAR_FOR_KASSASJON;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AVSLUTTET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createBaseSak;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createSakForAktoerId;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createSakForOrgNr;
import static no.nav.dokarkiv.core.util.TestdataFactory.GSAK_ORGNR;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedleggWithJournalstatusCode;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class AvsluttSakIT extends AbstractJournalpostIT {

	private static final String URL_AVSLUTT_SAK = "/rest/journalpostapi/v1/sak/avsluttSak";
	private static final String FAGSAK_ID = "0123A21";
	private static final String FAGSAK_SYSTEM = "IT01";
	private static final String TEMA = "SYK";
	private static final String AKTOER_ID = "1234567890123";
	private static final LocalDateTime JANUAR_2023 = LocalDateTime.of(2023, 1, 1, 1, 1);
	private static final LocalDateTime JANUAR_2010 = LocalDateTime.of(2010, 1, 1, 1, 1);
	private static final String ADMINISTRATIV_ENHET = "9999";
	private static final String SAKANSVARLIG = "Chandra Nalaar";
	private static final String KALLENDE_APP = "dokarkiv-itest";

	@Test
	public void happyPathAvsluttSakBruker() {
		setupStubs();
		long sakId = persistSakAndJournalpostForAktoerId(FS);

		var requestEntity = new HttpEntity<>(createAktoerIdAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertAvsluttetSak(updatedSak);
	}

	@Test
	public void happyPathAvsluttSakOrganisasjon() {
		setupStubs();
		long sakId = persistSakAndJournalpostForOrganisasjon(FS);

		var requestEntity = new HttpEntity<>(createOrganisasjonAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertAvsluttetSak(updatedSak);
	}

	@Test
	public void happyPathAvsluttSakWithNullStatus() {
		setupStubs();
		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID, FAGSAK_SYSTEM, FAGSAK_ID);
		sak.setSakStatus(null);
		Long sakId = sakTestRepository.persist(sak).getSakId();
		persistJournalpost(sakId, FS);

		var requestEntity = new HttpEntity<>(createAktoerIdAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertAvsluttetSak(updatedSak);
	}

	@Test
	public void shouldAvbryteSakWhenNoTilknyttedeJournalposter() {
		setupStubs();
		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID, FAGSAK_SYSTEM, FAGSAK_ID);
		Long sakId = sakTestRepository.persist(sak).getSakId();
		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createAktoerIdAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertAvbruttSak(updatedSak);
	}

	@Test
	public void shouldReturnBadRequestWhenNoSakFound() {
		setupStubs();

		var requestEntity = new HttpEntity<>(createAktoerIdAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString("Fant ingen saker for fagsakID=0123A21 og fagsaksystem=IT01"));
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"R", "D", "M", "MO", "OD"})
	public void shouldReturnBadRequestWhenJournalpostUnderRedigering(JournalStatusCode journalStatusCode) {
		setupStubs();
		persistSakAndJournalpostForAktoerId(journalStatusCode);

		var requestEntity = new HttpEntity<>(createAktoerIdAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString("Fagsystemsaken har en eller flere journalposter under redigering og kan ikke avsluttes."));
	}

	@Test
	public void shouldAvbryteSakWhenNoFerdigstilteJournalposter() {
		setupStubs();
		long sakId = persistSakAndJournalpostForAktoerId(A);

		var requestEntity = new HttpEntity<>(createAktoerIdAvsluttSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertAvbruttSak(updatedSak);
	}

	@Test
	public void shouldSetAdministrativEnhetAsSakAnsvarligWhenNoSakAnsvarlig() {
		setupStubs();
		long sakId = persistSakAndJournalpostForAktoerId(FS);

		AvsluttSakRequest avsluttSakRequest = createDefaultAvsluttSakRequest()
				.bruker(new Bruker(AKTOERID, AKTOER_ID))
				.sakAnsvarlig(null)
				.build();

		var requestEntity = new HttpEntity<>(avsluttSakRequest, createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertThat(updatedSak.getSakStatus(), is(AVSLUTTET));
		assertThat(updatedSak.getSakAnsvarlig(), is(ADMINISTRATIV_ENHET));
	}

	private void assertAvsluttetSak(Sak updatedSak) {
		assertThat(updatedSak.getSakStatus(), is(AVSLUTTET));
		assertThat(updatedSak.getAvleveringStatus(), is(nullValue()));
		assertThat(updatedSak.getKassasjonStatus(), is(nullValue()));
		assertThat(updatedSak.getEndretAv(), is(NAV_IDENT_SAKSBEHANDLER));
		assertThat(updatedSak.getEndretKildeNavn(), is(KALLENDE_APP));
		assertThat(updatedSak.getDatoEndret().getDay(), is(Date.from(now()).getDay()));
		assertThat(updatedSak.getDatoAvsluttet().getTime(), is(LocalDateTimeToDate(JANUAR_2023).getTime()));
		assertThat(updatedSak.getAvsluttetAv(), is(NAV_IDENT_SAKSBEHANDLER));
		assertThat(updatedSak.getAvsluttetKildeNavn(), is(KALLENDE_APP));
		assertThat(updatedSak.getDatoSakOpprettet().getTime(), is(LocalDateTimeToDate(JANUAR_2010).getTime()));
		assertThat(updatedSak.getAdministrativEnhet(), is(ADMINISTRATIV_ENHET));
		assertThat(updatedSak.getSakAnsvarlig(), is(SAKANSVARLIG));
	}

	private void assertAvbruttSak(Sak updatedSak) {
		assertThat(updatedSak.getSakStatus(), is(AVBRUTT));
		assertThat(updatedSak.getAvleveringStatus(), is(AvleveringStatusCode.AVBRUTT));
		assertThat(updatedSak.getKassasjonStatus(), is(KLAR_FOR_KASSASJON));
		assertThat(updatedSak.getEndretAv(), is(NAV_IDENT_SAKSBEHANDLER));
		assertThat(updatedSak.getDatoEndret().getDay(), is(Date.from(now()).getDay()));
	}

	private void setupStubs() {
		stubAzure();
		happyAktoerIdStub();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
	}

	private long persistSakAndJournalpostForAktoerId(JournalStatusCode statusCode) {
		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID, FAGSAK_SYSTEM, FAGSAK_ID);
		Long sakId = sakTestRepository.persist(sak).getSakId();

		persistJournalpost(sakId, statusCode);
		return sakId;
	}

	private void persistJournalpost(long sakId, JournalStatusCode statusCode) {
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedleggWithJournalstatusCode(sakId, statusCode);
		journalpostTestRepository.persist(journalpost);
		commitAndStartNewTransaction();
	}

	private long persistSakAndJournalpostForOrganisasjon(JournalStatusCode statusCode) {
		Sak sak = createSakForOrgNr(TEMA, GSAK_ORGNR, FAGSAK_SYSTEM, FAGSAK_ID);
		Long sakId = sakTestRepository.persist(sak).getSakId();
		persistJournalpost(sakId, statusCode);

		return sakId;
	}

	private AvsluttSakRequest.AvsluttSakRequestBuilder createDefaultAvsluttSakRequest() {
		return AvsluttSakRequest.builder()
				.avsluttetDato(JANUAR_2023)
				.sakAnsvarlig(SAKANSVARLIG)
				.tema(TEMA)
				.opprettetDato(JANUAR_2010)
				.fagsakId(FAGSAK_ID)
				.administrativEnhet(ADMINISTRATIV_ENHET)
				.fagsaksystem(FAGSAK_SYSTEM);
	}

	private AvsluttSakRequest createOrganisasjonAvsluttSakRequest() {
		return createDefaultAvsluttSakRequest()
				.bruker(new Bruker(ORGNR, GSAK_ORGNR))
				.build();
	}

	private AvsluttSakRequest createAktoerIdAvsluttSakRequest() {
		return createDefaultAvsluttSakRequest()
				.bruker(new Bruker(AKTOERID, AKTOER_ID))
				.build();
	}

	private Date LocalDateTimeToDate(LocalDateTime ldt) {
		return Date.from(ldt.toInstant(ZoneId.systemDefault().getRules().getOffset(now())));
	}
}
