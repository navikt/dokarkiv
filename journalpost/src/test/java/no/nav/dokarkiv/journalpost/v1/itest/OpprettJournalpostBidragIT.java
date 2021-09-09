package no.nav.dokarkiv.journalpost.v1.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus.KLAR_TIL_OVERFORING;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VEDLEGG_KVITTERING;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseBidragRequest;
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
	public void shouldMellomlagreBidragWhenTemaBidAndConsumerIdHeaderIsSet() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createRequestBidrag();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getJournalpostId()).isNotNull();
		BidragMellomlagring bidragMellomlagring = bidragMellomlagringRepository.findById(Long.parseLong(response.getBody().getJournalpostId().substring(4)))
				.orElseThrow(() -> new RuntimeException("Mellomlagring finnes ikke i databasen"));
		assertThat(bidragMellomlagring.getAvsenderFnr()).isEqualTo(AVSENDER_ID_PERSON);
		assertThat(bidragMellomlagring.getMottattDato()).isNotNull();
		assertThat(bidragMellomlagring.getStatus()).isEqualTo(KLAR_TIL_OVERFORING);

		BidragMellomlagringDokument hoveddokument = bidragMellomlagring.getBidragMellomlagringDokuments().stream().filter(p -> p.getDokumentType() == BidragMellomlagringDokumentType.HOVEDDOKUMENT).findFirst().get();
		BidragMellomlagringDokument vedlegg = bidragMellomlagring.getBidragMellomlagringDokuments().stream().filter(p -> p.getDokumentType() == BidragMellomlagringDokumentType.VEDLEGG).findFirst().get();
		BidragMellomlagringDokument vedleggKvittering = bidragMellomlagring.getBidragMellomlagringDokuments().stream().filter(p -> p.getDokumentType() == BidragMellomlagringDokumentType.VEDLEGG_KVITTERING).findFirst().get();
		assertThat(hoveddokument.getDokument()).isEqualTo(FYSISK_DOKUMENT);
		assertThat(vedlegg.getDokument()).isEqualTo(FYSISK_DOKUMENT_2);
		assertThat(vedleggKvittering.getDokument()).isEqualTo(VEDLEGG_KVITTERING);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertThat(response.getBody().getJournalpostId()).startsWith("4249");
		assertThat(response.getBody().getJournalpostferdigstilt()).isFalse();
		assertThat(response.getBody().getDokumenter()).hasSize(3);
	}

	@Test
	public void shouldNotMellomlagreBidragWhenTemaIsNotBid() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.tema(FagomradeCode.DAG.name())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenAvsenderMottakerIdNotSet() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(null)
						.idType(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenAvsenderMottakerIdTypeIsNotFNR() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id("999999999")
						.idType(ORGNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenAvsenderMottakerIdIsNot11Digits() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id("5798435a111")
						.idType(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenDatoMottattNotSet() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.datoMottatt(null)
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenDokumentNotVariantformatArkiv() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel("Tullesøknad")
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenNotAllDokumenterPdf() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Arrays.asList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build(),
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL2)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT_2)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenFysiskDokumentLength0() {
		abacPermit();
		restStsToken();

		OpprettJournalpostRequest request = createBaseBidragRequest()
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument("".getBytes())
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createBidragHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
}