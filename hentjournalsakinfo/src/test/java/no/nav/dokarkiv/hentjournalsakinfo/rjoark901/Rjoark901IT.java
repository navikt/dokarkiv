package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createBruker;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark901IT extends AbstractHentjournalsakinfoItest {

	private static final String HENTTILGANGJOURNALPOST_URI = "/hentjournalsakinfo/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}";
	private static final String EXPECTED_BRUKER_ID = "***gammelt_fnr***";

	@Test
	public void shouldGetTilgangJournalpost() {
		Journalpost storedJournalpost = persistJournalpost(createJournalpostWithHoveddokument());
		Long journalpostId = storedJournalpost.getJournalpostId();
		Long dokumentInfoId = storedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
				journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

		TilgangJournalpostDto responseJournalpost = responseEntity.getBody().getTilgangJournalpostDto();
		assertThat(responseJournalpost.getJournalpostId(), is(journalpostId.toString()));
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

		TilgangJournalpostDto responseJournalpost = responseEntity.getBody().getTilgangJournalpostDto();
		assertThat(responseJournalpost.getBruker().getBrukerId(), nullValue());
		assertThat(responseJournalpost.getBruker().getBrukerType(), nullValue());
	}

	@Test
	public void shouldGetTilgangJournalpostMultipleBrukereUsingLatestBruker() {
		Journalpost baseStoredJournalpost = persistJournalpost(createJournalpostWithHoveddokument());
		Bruker actualBruker = createBruker();
		actualBruker.setBrukerId(EXPECTED_BRUKER_ID);
		baseStoredJournalpost.addBruker(actualBruker);

		TestTransaction.start();
		Journalpost storedJournalpostTwoBrukere = persistJournalpost(baseStoredJournalpost);
		Long journalpostId = storedJournalpostTwoBrukere.getJournalpostId();
		Long dokumentInfoId = storedJournalpostTwoBrukere.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
				journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

		TilgangJournalpostDto responseJournalpost = responseEntity.getBody().getTilgangJournalpostDto();
		assertThat(responseJournalpost.getBruker().getBrukerId(), is(EXPECTED_BRUKER_ID));
	}

	@Test
	public void shouldReturn404WhenJournalpostDokumentInfoVariantTripletDoesNotExist() {
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), RestConsumerExceptionResponse.class,
				1L, 1L, VariantFormatCode.ARKIV.name());
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	private Journalpost persistJournalpost(Journalpost journalpost) {
		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		return journalpost;
	}
}
