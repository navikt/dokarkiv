package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.repository.DokumentInfoTestRepository;
import no.nav.dokarkiv.core.repository.SkannetInnholdTestRepository;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JournalfoerSkannetDokumentIT extends AbstractJournalpostIT {

	@Autowired
	SkannetInnholdTestRepository skannetInnholdTestRepository;
	@Autowired
	DokumentInfoTestRepository dokumentInfoTestRepository;

	private static final String LOGISK_VEDLEGG = "/logiskVedlegg/";
	private static final String NY_TITTEL = "Ny tittel";

	@Test
	public void shouldEndreLogiskVedlegg() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();
		Long logiskVedleggId = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentInfoId).getSkannetInnholdListe().iterator().next().getSkannetInnholdId();

		commitAndStartNewTransaction();

		EndreLogiskVedleggRequest request = EndreLogiskVedleggRequest.builder()
				.tittel(NY_TITTEL)
				.build();
		HttpEntity<EndreLogiskVedleggRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG + logiskVedleggId, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		commitAndStartNewTransaction();

		SkannetInnhold skannetInnhold = skannetInnholdTestRepository.findById(logiskVedleggId).orElse(null);

		assertThat(skannetInnhold).isNotNull();
		assertEquals(skannetInnhold.getVedleggInnhold(), NY_TITTEL);
	}

	@Test
	public void shouldLeggeTilLogiskVedlegg() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		LeggTilLogiskVedleggRequest request = LeggTilLogiskVedleggRequest.builder()
				.tittel(NY_TITTEL)
				.build();
		HttpEntity<LeggTilLogiskVedleggRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<LeggTilLogiskVedleggResponse> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG, HttpMethod.POST, requestEntity, LeggTilLogiskVedleggResponse.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		commitAndStartNewTransaction();

		SkannetInnhold skannetInnhold = skannetInnholdTestRepository.findById(parseLong(response.getBody().getLogiskVedleggId()))
				.orElse(null);

		assertThat(skannetInnhold).isNotNull();
		assertEquals(skannetInnhold.getVedleggInnhold(), NY_TITTEL);
	}

	@Test
	public void shouldSlettLogiskVedlegg() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();
		Long logiskVedleggId = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentInfoId).getSkannetInnholdListe().iterator().next().getSkannetInnholdId();

		commitAndStartNewTransaction();

		assertThat(skannetInnholdTestRepository.findById(logiskVedleggId)).isNotEmpty();

		HttpEntity<String> requestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG + logiskVedleggId, HttpMethod.DELETE, requestEntity, String.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		commitAndStartNewTransaction();

		SkannetInnhold skannetInnholdAfterDelete = skannetInnholdTestRepository.findById(logiskVedleggId).orElse(null);
		assertThat(skannetInnholdAfterDelete).isNull();
		assertThat(dokumentInfoTestRepository.findById(dokumentInfoId)).isNotEmpty();
	}

	@Test
	public void shouldBulkOppdatereLogiskVedlegg() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();
		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(List.of("Kvittering fra legekontor på konsultasjon", "Uttalelse fra lege"), createHeadersWithServiceUserToken());
		ResponseEntity<Void> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG, HttpMethod.PUT, requestEntity, Void.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<SkannetInnhold> byDokumentInfo = skannetInnholdTestRepository.findAllByDokumentInfo(dokumentInfoTestRepository.getReferenceById(dokumentInfoId));
		assertThat(byDokumentInfo)
				.extracting(SkannetInnhold::getVedleggInnhold)
				.containsExactly("Kvittering fra legekontor på konsultasjon", "Uttalelse fra lege");
	}
}
