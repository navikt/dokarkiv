package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Date;
import java.util.Objects;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createBruker;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoWithMoreData;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Rjoark901IT extends AbstractHentjournalsakinfoItest {

	private static final String HENTTILGANGJOURNALPOST_URI = "/hentjournalsakinfo/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}";
	private static final String EXPECTED_BRUKER_ID = "11111111111";

	@Test
	public void shouldGetTilgangJournalpost() {
		Journalpost storedJournalpost = persistJournalpost(createJournalpostWithHoveddokument());
		Long journalpostId = storedJournalpost.getJournalpostId();
		Long dokumentInfoId = storedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
				journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

		TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
		assertEquals(responseJournalpost.getJournalpostId(), journalpostId.toString());
	}

	@Test
	public void shouldGetTilgangJournalpostNoBruker() {
		Journalpost journalpostNoBrukere = createJournalpostWithHoveddokument();
		journalpostNoBrukere.clearBrukere();

		Journalpost storedJournalpost = persistJournalpost(journalpostNoBrukere);
		Long journalpostId = storedJournalpost.getJournalpostId();
		Long dokumentInfoId = storedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
				journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

		TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
		assertNull(responseJournalpost.getBruker().getBrukerId());
		assertNull(responseJournalpost.getBruker().getBrukerType());
	}

	@Test
	public void shouldGetTilgangJournalpostMultipleBrukereUsingLatestBruker() {
		Journalpost baseStoredJournalpost = saveJournalpost(createJournalpostWithHoveddokument());
		Bruker actualBruker = createBruker();
		actualBruker.setBrukerId(EXPECTED_BRUKER_ID);
		baseStoredJournalpost.addBruker(actualBruker);

		Journalpost storedJournalpostTwoBrukere = journalpostTestRepository.merge(baseStoredJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Long journalpostId = storedJournalpostTwoBrukere.getJournalpostId();
		Long dokumentInfoId = storedJournalpostTwoBrukere.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
				journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

		TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
		assertEquals(EXPECTED_BRUKER_ID, responseJournalpost.getBruker().getBrukerId());
	}

	@Test
	public void shouldGetTilgangJournalpostWithMoreData() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.setJournalDato(new Date());
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, createDokumentInfoWithMoreData()));
		persistJournalpost(journalpost);

		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).stream().findFirst().get().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
				journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

		TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
		TilgangDokumentInfoDto tilgangDokumentInfoDto = responseJournalpost.getDokument();

		assertFalse(tilgangDokumentInfoDto.getKassert());
		assertFalse(responseJournalpost.getSak().getFeilregistrert());
		assertEquals(DokumentKategoriCode.B, tilgangDokumentInfoDto.getKategori());
		assertNotNull(responseJournalpost.getJournalfoertDato());
	}

	@Test
	public void shouldReturn404WhenJournalpostDokumentInfoVariantTripletDoesNotExist() {
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), RestConsumerExceptionResponse.class,
				1L, 1L, VariantFormatCode.ARKIV.name());
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
		assertEquals("Ingen journalpost funnet for journalpostId=1, dokumentInfoId=1, variantFormat=ARKIV", Objects.requireNonNull(responseEntity.getBody()).getMessage());

	}

	private Journalpost persistJournalpost(Journalpost journalpost) {
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		return journalpost;
	}
}
