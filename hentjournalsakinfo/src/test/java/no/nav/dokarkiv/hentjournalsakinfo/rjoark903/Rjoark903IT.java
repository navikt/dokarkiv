package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Rjoark903IT extends AbstractHentjournalsakinfoItest {
	private static final String TILKNYTTEDEJOURNALPOSTER_GJENBRUK = "/hentjournalsakinfo/tilknyttedejournalposter/{dokumentInfoId}/GJENBRUK";
	private static final String ANTALL_RETUR = "3";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(100L);
		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(0));
	}

	@Test
	public void shouldReturnGjenbrukteJournalposter() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		Journalpost gjenbrukt = createJournalpostWithGjenbruktHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		joarkRepository.save(journalpost);
		joarkRepository.save(gjenbrukt);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId());
		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(2));
	}

	@Test
	public void shouldReturnJournalpostWithNormalTilknytning() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId());
		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(1));
		assertEquals(responseEntity.getBody().getTilknyttedeJournalposter().get(0).getAntallRetur(), ANTALL_RETUR);
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.save(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.save(vedlegg1);
		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		joarkRepository.save(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<TilknyttedeJournalposterResponse> responseEntity = tilknyttedeJournalposterGjenbrukRest(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId());

		assertThat(responseEntity.getBody().getTilknyttedeJournalposter(), hasSize(1));
		TilknyttetJournalpostDto journalpostDto = responseEntity.getBody().getTilknyttedeJournalposter().get(0);
		assertThat(journalpostDto.getDokumenter(), hasSize(3));
		assertThat(journalpostDto.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
	}

	private ResponseEntity<TilknyttedeJournalposterResponse> tilknyttedeJournalposterGjenbrukRest(final Long dokumentInfoId) {
		return restTemplate.exchange(TILKNYTTEDEJOURNALPOSTER_GJENBRUK, HttpMethod.GET, createHeaderEntity(), TilknyttedeJournalposterResponse.class, dokumentInfoId);
	}
}
