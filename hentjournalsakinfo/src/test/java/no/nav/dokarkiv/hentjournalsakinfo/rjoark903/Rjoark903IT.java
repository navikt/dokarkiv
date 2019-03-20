package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark903IT extends AbstractHentjournalsakinfoItest {
	private static final String TILKNYTTEDEJOURNALPOSTER_GJENBRUK = "/hentjournalsakinfo/tilknyttedejournalposter/{dokumentInfoId}/GJENBRUK";


	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(100L);
		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(0));
	}

	@Test
	public void shouldReturnGjenbrukteJournalposter() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		Journalpost gjenbrukt = createJournalpostWithGjenbruktHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		joarkRepository.save(journalpost);
		joarkRepository.save(gjenbrukt);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId());
		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(2));
	}

	@Test
	public void shouldReturnJournalpostWithNormalTilknytning() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId());
		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(1));
	}

	private ResponseEntity<TilknyttedeJournalposterResponse> tilknyttedeJournalposterGjenbrukRest(final Long dokumentInfoId) {
		return restTemplate.exchange(TILKNYTTEDEJOURNALPOSTER_GJENBRUK, HttpMethod.GET, createHeaderEntity(), TilknyttedeJournalposterResponse.class, dokumentInfoId);
	}
}
