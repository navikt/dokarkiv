package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.SafinternConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptySet;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createDokumentInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFysiskpostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createHoveddokumentRelasjonGjenbruktDokumentInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.formattedDate;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.setSkjermingVedlegg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class TilknyttetJournalpostIT extends AbstractSafinternTest {

	@Test
	void shouldGetJournalposterTilknyttetDokumentInfoId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost opprinneligJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		Journalpost gjenbrukendeJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		gjenbrukendeJournalpost.setKanalReferanseId("En annen referanseId");
		gjenbrukendeJournalpost.getJournalpostDokumentInfoRelasjoner().forEach(gjenbrukendeJournalpost::removeJournalpostDokumentInfoRelasjon);
		gjenbrukendeJournalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjonGjenbruktDokumentInfo(gjenbrukendeJournalpost, opprinneligJournalpost.getDokumentInfoFromJpDokInfoRelasjoner(0)));
		opprinneligJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		setSkjermingVedlegg(opprinneligJournalpost);
		gjenbrukendeJournalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		Journalpost persistedJournalpost = journalpostTestRepository.persist(opprinneligJournalpost);
		journalpostTestRepository.persist(gjenbrukendeJournalpost);
		UtsendingsInfo utsendingsInfo = createFysiskpostUtsendingsInfo(opprinneligJournalpost);
		utsendingsInfoTestRepository.persist(utsendingsInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		ResponseEntity<String> responseEntity = restTemplate.exchange(tilknyttedeJournalposterPath(dokumentInfoId), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(persistedJournalpost, gjenbrukendeJournalpost, classpathResourceToString("/tilknyttetjournalpost/journalpost-dokumenter-response.json")));
	}

	@Test
	void shouldGet404WhenDokumentInfoPresentButNotJournalposts() {
		DokumentInfo dokumentInfo = createDokumentInfo(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		DokumentInfo persistedDokumentInfo = dokumentInfoTestRepository.persist(dokumentInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = persistedDokumentInfo.getDokumentInfoId();
		ResponseEntity<String> responseEntity = restTemplate.exchange(tilknyttedeJournalposterPath(dokumentInfoId), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).containsIgnoringCase("Fant ingen Journalpost tilknyttet dokumentInfoId=" + dokumentInfoId);
	}

	@Test
	void shouldGetJournalposterTilknyttetDokumentInfoIdWithOnlySelectedFields() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost opprinneligJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		Journalpost gjenbrukendeJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		gjenbrukendeJournalpost.setKanalReferanseId("En annen referanseId");
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

		Set<String> fields = Set.of("journalpostId", "saksrelasjon");
		Long dokumentInfoId = persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		ResponseEntity<String> responseEntity = restTemplate.exchange(tilknyttedeJournalposterPath(dokumentInfoId, fields), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(persistedJournalpost, gjenbrukendeJournalpost, classpathResourceToString("/tilknyttetjournalpost/journalpost-dokumenter-jpid-og-saksrelasjon-response.json")));
	}

	@Test
	void shouldGet400WhenRequestingNoinExistentFieldValue() {
		DokumentInfo dokumentInfo = createDokumentInfo(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		DokumentInfo persistedDokumentInfo = dokumentInfoTestRepository.persist(dokumentInfo);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = persistedDokumentInfo.getDokumentInfoId();
		Set<String> fields = Set.of("journalpostIdentifikasmultiplikasjon", "saksrelasjon");
		ResponseEntity<String> responseEntity = restTemplate.exchange(tilknyttedeJournalposterPath(dokumentInfoId, fields), HttpMethod.GET, createHeaderEntityMedTilgang(), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody()).containsIgnoringCase("forsøker fetch på ugyldig path=journalpostIdentifikasmultiplikasjon");
	}

	String tilknyttedeJournalposterPath(long dokumentInfoId) {
		return tilknyttedeJournalposterPath(dokumentInfoId, emptySet());
	}

	String tilknyttedeJournalposterPath(long dokumentInfoId, Set<String> fields) {
		if (fields.isEmpty()) {
			return SafinternConstants.BASE_PATH + "/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/%s".formatted(dokumentInfoId);
		}
		return SafinternConstants.BASE_PATH + "/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/%d?fields=%s".formatted(dokumentInfoId, String.join(",", fields));
	}

	private static String mapStringResponse(Journalpost originalJournalpost, Journalpost gjenbrukendeJournalpost, String responseTemplate) {
		Date createdDate = originalJournalpost.getChangeStamp().getCreatedDate();
		String nowIso = formattedDate().toFormatter().format(createdDate.toInstant().atZone(ZoneId.of("UTC"))) + "+00:00";
		Date createdDateGjenbrukt = gjenbrukendeJournalpost.getChangeStamp().getCreatedDate();
		String gjenbruktNowIso = formattedDate().toFormatter().format(createdDateGjenbrukt.toInstant().atZone(ZoneId.of("UTC"))) + "+00:00";
		DokumentInfo hoved = originalJournalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(JournalpostDokumentInfoRelasjon::isHoveddokument)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo).findFirst().get();
		DokumentInfo vedlegg = originalJournalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(JournalpostDokumentInfoRelasjon::isVedlegg)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo).findFirst().get();
		assertThat(hoved).isNotNull();
		assertThat(vedlegg).isNotNull();
		return responseTemplate
				.replace("opprettet_replace", nowIso)
				.replace("opprettet_b_replace", gjenbruktNowIso)
				.replace("referanseId_replace", originalJournalpost.getKanalReferanseId())
				.replace("referanseId_b_replace", gjenbrukendeJournalpost.getKanalReferanseId())
				.replace("journalpostId_replace", originalJournalpost.getJournalpostId().toString())
				.replace("journalpostId_b_replace", gjenbrukendeJournalpost.getJournalpostId().toString())
				.replace("dokumentInfoId_hoveddokument_replace", hoved.getDokumentInfoId().toString())
				.replace("dokumentInfoId_vedlegg_replace", vedlegg.getDokumentInfoId().toString())
				.replace("sakId_replace", originalJournalpost.getSaksrelasjon().getSakId().toString())
				.replace("logiskVedleggId_hoveddokument_replace", hoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
				.replace("logiskVedleggId_vedlegg_replace", vedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString());
	}
}
