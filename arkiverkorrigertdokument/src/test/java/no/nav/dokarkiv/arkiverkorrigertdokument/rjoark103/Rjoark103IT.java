package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.arkiverkorrigertdokument.AbstractArkiverKorrigertDokumentIT;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark103IT extends AbstractArkiverKorrigertDokumentIT {

	@Test
	public void funkerTest() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ARKIVERKORRIGERTDOKUMENT + journalpost.getJournalpostId() + "/" + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertTrue(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId()).isPresent());
	}



}
