package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
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

import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFysiskpostUtsendingsInfo;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
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

	private String mapStringResponse(String path, Journalpost journalpost) {
		Date createdDate = journalpost.getChangeStamp().getCreatedDate();
		String nowIso = formattedDate().toFormatter().format(createdDate.toInstant().atZone(ZoneId.of("UTC"))) + "+00:00";
		DokumentInfo hoved = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).iterator().next().getDokumentInfo();
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

	String journalpostIdPath(Long journalpostId) {
		return SafinternConstants.BASE_PATH + "/journalpost/journalpostId/%d".formatted(journalpostId);
	}

	String eksternReferanseIdPath(String eksternReferanseId) {
		return SafinternConstants.BASE_PATH + "/journalpost/eksternReferanseId/%s".formatted(eksternReferanseId);
	}
}
