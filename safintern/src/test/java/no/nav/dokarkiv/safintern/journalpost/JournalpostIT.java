package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.SafinternConstants;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFysiskpostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createHoveddokumentRelasjonGjenbruktDokumentInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.formattedDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class JournalpostIT extends AbstractSafinternTest {

	@Test
	void shouldGetJournalpostByJournalpostId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(journalpostIdPath(actualJournalpost.getJournalpostId()), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse("/journalpost/journalpost-response.json", persistedJournalpost));
	}

	@Test
	void shouldGetJournalpostByJournalpostIdWithFields() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(actualJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		var fields = Set.of("journalpostId", "saksrelasjon");
		ResponseEntity<String> responseEntity = restTemplate.exchange(journalpostIdPath(actualJournalpost.getJournalpostId(), fields), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse("/journalpost/journalpost-response-fields.json", persistedJournalpost));
	}

	@Test
	void shouldGetJournalpostByEksternReferanseId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(eksternReferanseIdPath(KANAL_REFERANSE_ID), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse("/journalpost/journalpost-response.json", persistedJournalpost));
	}

	@Test
	void shouldGetJournalpostByIdAndDokumentInfoId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(actualJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(actualJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = actualJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		ResponseEntity<String> responseEntity = restTemplate.exchange(journalpostIdDokumentInfoIdPath(actualJournalpost.getJournalpostId(), dokumentInfoId), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse("/journalpost/journalpost-dokument-response.json", persistedJournalpost));
	}

	/**
	 * Fields brukes av saf til å hente tilgang metadata. Viktig at alle felt er med da et feilaktig null-felt kan gi tilgang da det ikke burde
	 */
	@Test
	void shouldGetJournalpostByIdAndDokumentInfoIdWithSafTilgangFetches() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.findHoveddokumentDokumentInfoRelasjon().setSkjermingType(POL);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(actualJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = actualJournalpost.getJournalpostDokumentInfoRelasjonerAdmin()
				.stream().filter(JournalpostDokumentInfoRelasjon::isHoveddokument).findFirst().get().getDokumentInfo().getDokumentInfoId();
		var safHentDokumentTilgangFields = Set.of("journalpostId", "fagomraade", "status", "skjerming", "bruker", "saksrelasjon", "dokumenter.dokumentInfoId", "dokumenter.skjerming", "dokumenter.fildetaljer");
		ResponseEntity<String> responseEntity = restTemplate.exchange(journalpostIdDokumentInfoIdPath(actualJournalpost.getJournalpostId(), dokumentInfoId, safHentDokumentTilgangFields), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse("/journalpost/journalpost-dokument-saf-tilgang-response.json", persistedJournalpost));
	}

	/**
	 * Fields brukes av safselvbetjening til å hente tilgang metadata. Viktig at alle felt er med da et feilaktig null-felt kan gi tilgang da det ikke burde
	 */
	@Test
	void shouldGetJournalpostByIdAndDokumentInfoIdWithSafselvbetjeningTilgangFetches() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost actualJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		actualJournalpost.findHoveddokumentDokumentInfoRelasjon().setSkjermingType(POL);
		actualJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(actualJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = actualJournalpost.getJournalpostDokumentInfoRelasjonerAdmin()
				.stream().filter(JournalpostDokumentInfoRelasjon::isHoveddokument).findFirst().get().getDokumentInfo().getDokumentInfoId();
		var safselvbetjeningHentDokumentTilgangFields = Set.of("journalpostId", "fagomraade", "status", "type", "skjerming", "mottakskanal", "innsyn", "utsendingskanal",
				"bruker", "avsenderMottaker", "relevanteDatoer", "saksrelasjon",
				"dokumenter.dokumentInfoId", "dokumenter.tilknyttetSom", "dokumenter.kassert", "dokumenter.kategori", "dokumenter.skjerming", "dokumenter.fildetaljer");
		ResponseEntity<String> responseEntity = restTemplate.exchange(journalpostIdDokumentInfoIdPath(actualJournalpost.getJournalpostId(), dokumentInfoId, safselvbetjeningHentDokumentTilgangFields), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse("/journalpost/journalpost-dokument-safselvbetjening-tilgang-response.json", persistedJournalpost));
	}

	private String mapStringResponse(String path, Journalpost journalpost) {
		Date createdDate = journalpost.getChangeStamp().getCreatedDate();
		String nowIso = formattedDate().toFormatter().format(createdDate.toInstant().atZone(ZoneId.of("UTC"))) + "+00:00";
		DokumentInfo hoved = journalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(JournalpostDokumentInfoRelasjon::isHoveddokument)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo).findFirst().get();
		DokumentInfo vedlegg = journalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(JournalpostDokumentInfoRelasjon::isVedlegg)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo).findFirst().get();
		assertThat(hoved).isNotNull();
		assertThat(vedlegg).isNotNull();
		return classpathResourceToString(path)
				.replace("opprettet_replace", nowIso)
				.replace("journalpostId_replace", journalpost.getJournalpostId().toString())
				.replace("dokumentInfoId_hoveddokument_replace", hoved.getDokumentInfoId().toString())
				.replace("dokumentInfoId_vedlegg_replace", vedlegg.getDokumentInfoId().toString())
				.replace("sakId_replace", journalpost.getSaksrelasjon().getSakId().toString())
				.replace("logiskVedleggId_hoveddokument_replace", hoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
				.replace("logiskVedleggId_vedlegg_replace", vedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString());
	}

	@Test
	void shouldReturnNotFoundWhenJournalpostIdNotFound() {
		ResponseEntity<String> responseEntity = restTemplate.exchange(journalpostIdPath(123L), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).contains("Journalpost med journalpostId=123 ikke funnet");
	}

	@Test
	void shouldReturnNotFoundWhenEksternReferanseIdNotFound() {
		ResponseEntity<String> responseEntity = restTemplate.exchange(eksternReferanseIdPath("ekstern"), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).contains("Journalpost med eksternReferanseId=ekstern ikke funnet");
	}

	@Test
	void shouldGetJournalposterTilknyttetDokumentInfoId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost opprinneligJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		Journalpost gjenbrukendeJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		gjenbrukendeJournalpost.setKanalReferanseId("carnal referanseid");
		gjenbrukendeJournalpost.getJournalpostDokumentInfoRelasjoner().forEach(gjenbrukendeJournalpost::removeJournalpostDokumentInfoRelasjon);
		gjenbrukendeJournalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjonGjenbruktDokumentInfo(gjenbrukendeJournalpost, opprinneligJournalpost.getDokumentInfoFromJpDokInfoRelasjoner(0)));
		opprinneligJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		gjenbrukendeJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(opprinneligJournalpost);
		journalpostTestRepository.persist(gjenbrukendeJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(opprinneligJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = opprinneligJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		ResponseEntity<List<MinimalAssertableJournalpost>> responseEntity = restTemplate.exchange(tilknyttedeJournalposterPath(dokumentInfoId), HttpMethod.GET, createHeaderEntityMedTilgang(), new ParameterizedTypeReference<>() {
		});

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).hasSize(2);
		assertThat(responseEntity.getBody()).has(
				new Condition<List>(o -> o.stream().anyMatch(x -> {
					MinimalAssertableJournalpost assertedJP = (MinimalAssertableJournalpost) x;
					return gjenbrukendeJournalpost.getJournalpostId().equals(assertedJP.journalpostId()) &&
								assertedJP.dokumenter().size() == 1 &&
								assertedJP.dokumenter().get(0).originalJournalpostId().equals(opprinneligJournalpost.getJournalpostId());
				}), "respons inneholder gjenbrukendeJournalpost og den har akkurat ett dokument og originalJournalpostId for dokumentet tilhører den opprinnelige journalposten"));

		List<Long> dokumentIds = responseEntity.getBody().stream()
				.map(MinimalAssertableJournalpost::dokumenter)
				.flatMap(List::stream)
				.map(MinimalAssertableDokumentInfo::dokumentInfoId).toList();
		assertThat(dokumentIds).hasSize(2);
		assertThat(dokumentIds.get(0)).isEqualTo(dokumentIds.get(1));

	}

	String journalpostIdPath(Long journalpostId) {
		return journalpostIdPath(journalpostId, Set.of());
	}

	String journalpostIdPath(Long journalpostId, Set<String> fields) {
		if (fields.isEmpty()) {
			return SafinternConstants.BASE_PATH + "/journalpost/journalpostId/%d".formatted(journalpostId);
		} else {
			return SafinternConstants.BASE_PATH + "/journalpost/journalpostId/%d?fields=%s"
					.formatted(journalpostId, String.join(",", fields));
		}
	}

	String eksternReferanseIdPath(String eksternReferanseId) {
		return SafinternConstants.BASE_PATH + "/journalpost/eksternReferanseId/%s".formatted(eksternReferanseId);
	}

	String journalpostIdDokumentInfoIdPath(Long journalpostId, Long dokumentInfoId) {
		return SafinternConstants.BASE_PATH + "/journalpost/journalpostId/%d/dokumentInfoId/%d".formatted(journalpostId, dokumentInfoId);
	}

	String journalpostIdDokumentInfoIdPath(Long journalpostId, Long dokumentInfoId, Set<String> fields) {
		return SafinternConstants.BASE_PATH + "/journalpost/journalpostId/%d/dokumentInfoId/%d?fields=%s"
				.formatted(journalpostId, dokumentInfoId, String.join(",", fields));
	}

	String tilknyttedeJournalposterPath(long dokumentInfoId) {
		return SafinternConstants.BASE_PATH + "/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/%s".formatted(dokumentInfoId);
	}

	record MinimalAssertableJournalpost(Long journalpostId, List<MinimalAssertableDokumentInfo> dokumenter) {}
	record MinimalAssertableDokumentInfo(Long dokumentInfoId, Long originalJournalpostId) {}

}
