package no.nav.dokarkiv.journalpost.v1.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfoId;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import java.io.IOException;

import static no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus.KLAR_TIL_OVERFORING;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DATO_MOTTATT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequestBidrag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class OpprettJournalpostBidragIT extends AbstractJournalpostIT {

	@Inject
	private BidragMellomlagringRepository bidragMellomlagringRepository;

	@Before
	public void setUp() {
		WireMock.reset();
	}

	@Test
	public void shouldMellomlagreBidragWhenTemaBidAndConsumerIdHeaderIsSet() throws IOException {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createRequestBidrag();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertThat(response.getBody().getJournalpostId()).isNotNull();
		BidragMellomlagring bidragMellomlagring = bidragMellomlagringRepository.findById(Long.parseLong(response.getBody().getJournalpostId().substring(4)))
				.orElseThrow(() -> new RuntimeException("Mellomlagring finnes ikke i databasen"));
		assertThat(bidragMellomlagring.getAvsenderFnr()).isEqualTo(AVSENDER_ID_PERSON);
		assertThat(bidragMellomlagring.getMottattDato()).isEqualTo(DATO_MOTTATT);
		assertThat(bidragMellomlagring.getStatus()).isEqualTo(KLAR_TIL_OVERFORING);

		BidragMellomlagringDokument hoveddokument = bidragMellomlagring.getBidragMellomlagringDokuments().stream().filter(p -> p.getDokumentType() == BidragMellomlagringDokumentType.HOVEDDOKUMENT).findFirst().get();
		BidragMellomlagringDokument vedlegg = bidragMellomlagring.getBidragMellomlagringDokuments().stream().filter(p -> p.getDokumentType() == BidragMellomlagringDokumentType.VEDLEGG).findFirst().get();;
		assertThat(hoveddokument.getDokument()).isEqualTo(FYSISK_DOKUMENT);
		assertThat(vedlegg.getDokument()).isEqualTo(FYSISK_DOKUMENT_2);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertThat(response.getBody().getJournalpostId()).startsWith("4249");
		assertThat(response.getBody().getJournalpostferdigstilt()).isFalse();
		assertThat(response.getBody().getDokumenter()).hasSize(2);
		assertThat(response.getBody().getDokumenter()).extracting(DokumentInfoId::getDokumentInfoId).startsWith("4249");
	}
}