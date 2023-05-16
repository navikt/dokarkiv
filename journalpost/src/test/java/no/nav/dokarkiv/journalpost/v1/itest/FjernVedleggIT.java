package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostUnderArbeid;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createVedleggRelasjon;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class FjernVedleggIT extends AbstractJournalpostIT {

	private static final String FJERNVEDLEGG = "/fjernVedlegg";

	@Test
	public void shouldHappyFjernVedleggTilknyttJournalpost() {
		abacPermit();

		Journalpost journalpost1 = createJournalpostUnderArbeid();
		Journalpost journalpost2 = createJournalpostUnderArbeid();
		Journalpost journalpostSomSkalFjernes = createJournalpostUnderArbeid();

		DokumentInfo dokumentInfo = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		journalpostSomSkalFjernes.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpostSomSkalFjernes, dokumentInfo));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		Long journalpostId = saveJournalpost(journalpostSomSkalFjernes).getJournalpostId();
		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonPersist = journalpostDokumentInfoRelasjonTestRepository.findAll();
		JournalpostDokumentInfoRelasjon vedllegJpDokumentInfoRelasjon = journalpostDokumentInfoRelasjonTestRepository.findAll().stream()
				.filter(jpdok -> VEDLEGG.equals(jpdok.getTilknyttetJournalpostSom()))
				.findAny()
				.get();
		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonByJpBeforeDelete = journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(vedllegJpDokumentInfoRelasjon.getJournalpost().getJournalpostId());

		List<Journalpost> persitJournalpost = journalpostTestRepository.findAll();
		assertThat(persitJournalpost.size(), is(3));
		assertThat(jpDokInfoRelasjonPersist.size(), is(4));
		assertThat(jpDokInfoRelasjonByJpBeforeDelete.size(), is(2));

		FjernVedleggTilknyttetJournalpostRequest request = FjernVedleggTilknyttetJournalpostRequest.builder()
				.dokumentId(vedllegJpDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId().toString())
				.build();


		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FJERNVEDLEGG, HttpMethod.PATCH, requestEntity, String.class);

		commitAndStartNewTransaction();
		Optional<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjon = journalpostDokumentInfoRelasjonTestRepository.findById(vedllegJpDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId());
		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonByJp = journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(vedllegJpDokumentInfoRelasjon.getJournalpost().getJournalpostId());
		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonAfterDelete = journalpostDokumentInfoRelasjonTestRepository.findAll();
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertThat(jpDokInfoRelasjon.isPresent(), is(false));
		assertThat(jpDokInfoRelasjonByJp, notNullValue());
		assertThat(jpDokInfoRelasjonByJp.size(), is(1));
		assertThat(jpDokInfoRelasjonAfterDelete.size(), is(3));
	}

	@Test
	public void shouldFailToFjernVedleggJournalpostEqualsWithDokumentInfoOriginalJournalpostWithStatus4XX() {
		abacPermit();

		Journalpost journalpost1 = createJournalpostUnderArbeid();
		Journalpost journalpost2 = createJournalpostUnderArbeid();
		Journalpost journalpostSomSkalFjernes = createJournalpostUnderArbeid();

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpostSomSkalFjernes);
		commitAndStartNewTransaction();

		JournalpostDokumentInfoRelasjon vedllegJpDokumentInfoRelasjon = journalpostDokumentInfoRelasjonTestRepository.findAll().get(2);
		List<Journalpost> persitJournalpost = journalpostTestRepository.findAll();
		Long journalpostId = vedllegJpDokumentInfoRelasjon.getJournalpost().getJournalpostId();
		FjernVedleggTilknyttetJournalpostRequest request = FjernVedleggTilknyttetJournalpostRequest.builder()
				.dokumentId(vedllegJpDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId().toString())
				.build();
		assertThat(persitJournalpost.size(), is(3));

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FJERNVEDLEGG, HttpMethod.PATCH, requestEntity, String.class);
		commitAndStartNewTransaction();

		Optional<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjon = journalpostDokumentInfoRelasjonTestRepository.findById(vedllegJpDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId());
		assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(jpDokInfoRelasjon.isPresent(), is(true));
	}

	@Test
	public void shouldReturnNotFoundWhenJournalpostNotFound() {
		abacPermit();

		commitAndStartNewTransaction();
		FjernVedleggTilknyttetJournalpostRequest request = FjernVedleggTilknyttetJournalpostRequest.builder()
				.dokumentId("1111")
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + JOURNALPOST_ID + FJERNVEDLEGG, HttpMethod.PATCH, requestEntity, String.class);
		commitAndStartNewTransaction();

		assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void shouldReturnBadRequestWhenDokumentInfoIdNull() {
		abacPermit();

		commitAndStartNewTransaction();
		FjernVedleggTilknyttetJournalpostRequest request = FjernVedleggTilknyttetJournalpostRequest.builder()
				.dokumentId(null)
				.build();

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + JOURNALPOST_ID + FJERNVEDLEGG, HttpMethod.PATCH, requestEntity, String.class);
		commitAndStartNewTransaction();

		assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
	}
}
