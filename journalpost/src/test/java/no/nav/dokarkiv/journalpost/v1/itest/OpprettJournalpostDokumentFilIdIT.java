package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.datautil.SakTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import no.nav.dokarkiv.core.repository.DokumentFilOpplastingTestRepository;
import no.nav.dokarkiv.core.storage.InMemoryBucketStorage;
import no.nav.dokarkiv.core.util.Crc32c;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.ARKIVERES;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.ARKIVERT;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTET_OPP;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ORIGINAL;
import static no.nav.dokarkiv.core.util.Digest.sha256;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_LAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BATCHNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2_SHA256_DIGEST;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_SHA256_DIGEST;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

public class OpprettJournalpostDokumentFilIdIT extends AbstractJournalpostIT {

	private static final UUID DOKUMENT_FIL_ID_1 = UUID.fromString("1f1c62f0-0595-6fd4-86dd-a82bdd6dfe78");
	private static final UUID DOKUMENT_FIL_ID_2 = UUID.fromString("1f1c62f5-9623-6108-9311-a82bdd6dfe78");
	private static final String EKSTERN_DOKUMENT_REFERANSE_ID_1 = "0a4ba1eb-aab8-4073-bcde-5e1313ae73e3";
	private static final String EKSTERN_DOKUMENT_REFERANSE_ID_2 = "1a1c907b-4759-4431-b7b7-66d3e9ef690f";
	private static final Map<String, List<String>> FERDIGSTILL_QUERY = Map.of("forsoekFerdigstill", List.of("true"));

	@Autowired
	private InMemoryBucketStorage dokarkivMellomlagerBucketStorage;
	@Autowired
	private DokumentFilOpplastingTestRepository dokumentFilOpplastingTestRepository;

	@AfterEach
	void tearDown() {
		dokarkivMellomlagerBucketStorage.clear();
	}

	@Test
	public void happyPathOpprettInngaaendeWithDokumentFilId() {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, null, SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findById(Long.valueOf(response.getBody().getJournalpostId())).orElseThrow();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());
		assertEquals(AvsenderMottakerIdTypeCode.FNR, journalpost.getAvsenderMottakerIdType());
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_LAND, journalpost.getLand());
		assertNotNull(journalpost.getDokumentDato());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.getFirst().getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.getFirst().getAksjon());
		assertThat(aksjonsLoggList.getFirst().getArkivElementEndringer()).hasSize(5);

		DokumentInfo hovedDokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		assertEquals(DOKUMENT_TITTEL1, hovedDokumentInfo.getTittel());
		assertEquals(BREVKODE1, hovedDokumentInfo.getBrevkode());
		FilDetaljer hovedDokumentOriginal = hovedDokumentInfo.findFilDetaljerByVariantFormat(ORIGINAL);
		assertEquals(FILNAVN, hovedDokumentOriginal.getFilnavn());
		assertEquals(FYSISK_DOKUMENT_2.length, parseInt(hovedDokumentOriginal.getFilstorrelse()));
		assertArrayEquals(FYSISK_DOKUMENT_2_SHA256_DIGEST, hovedDokumentOriginal.getSha256Sjekksum());
		DokumentFil hovedDokumentFilOriginal = dokumentFilTestRepository.findByFilUuid(hovedDokumentOriginal.getFilUuid());
		assertArrayEquals(FYSISK_DOKUMENT_2, hovedDokumentFilOriginal.getFil());

		FilDetaljer hovedDokumentArkiv = hovedDokumentInfo.findFilDetaljerByVariantFormat(ARKIV);
		assertEquals(DOKUMENT_FIL_ID_1.toString(), hovedDokumentArkiv.getFilUuid());
		assertEquals(BATCHNAVN, hovedDokumentArkiv.getBatchNavn());
		assertEquals(EKSTERN_DOKUMENT_REFERANSE_ID_1, hovedDokumentArkiv.getEksternDokumentReferanseId());
		assertEquals(FYSISK_DOKUMENT.length, parseInt(hovedDokumentArkiv.getFilstorrelse()));
		assertArrayEquals(FYSISK_DOKUMENT_SHA256_DIGEST, hovedDokumentArkiv.getSha256Sjekksum());
		DokumentFil hovedDokumentFilArkiv = dokumentFilTestRepository.findByFilUuid(hovedDokumentArkiv.getFilUuid());
		assertNull(hovedDokumentFilArkiv);

		Set<JournalpostDokumentInfoRelasjon> vedleggRelasjons = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG);
		DokumentInfo vedleggDokumentInfo = vedleggRelasjons.iterator().next().getDokumentInfo();
		assertEquals(DOKUMENT_TITTEL2, vedleggDokumentInfo.getTittel());
		assertEquals(BREVKODE2, vedleggDokumentInfo.getBrevkode());
		FilDetaljer vedleggArkiv = vedleggDokumentInfo.findFilDetaljerByVariantFormat(ARKIV);
		assertEquals(DOKUMENT_FIL_ID_2.toString(), vedleggArkiv.getFilUuid());
		assertEquals(BATCHNAVN, vedleggArkiv.getBatchNavn());
		assertEquals(EKSTERN_DOKUMENT_REFERANSE_ID_2, vedleggArkiv.getEksternDokumentReferanseId());
		assertEquals(FYSISK_DOKUMENT_2.length, parseInt(vedleggArkiv.getFilstorrelse()));
		assertArrayEquals(FYSISK_DOKUMENT_2_SHA256_DIGEST, vedleggArkiv.getSha256Sjekksum());
		DokumentFil vedleggFilArkiv = dokumentFilTestRepository.findByFilUuid(vedleggArkiv.getFilUuid());
		assertNull(vedleggFilArkiv);

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner())
				.extracting(JournalpostDokumentInfoRelasjon::getTilknyttetJournalpostSom, JournalpostDokumentInfoRelasjon::getRekkefoelge)
				.containsExactly(
						tuple(HOVEDDOKUMENT, null),
						tuple(VEDLEGG, 2)
				);

		assertDokumentFilOpplastingArkiveres(DOKUMENT_FIL_ID_1, FYSISK_DOKUMENT);
		assertDokumentFilOpplastingArkiveres(DOKUMENT_FIL_ID_2, FYSISK_DOKUMENT_2);
		assertDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	@Test
	public void happyPathOpprettOgFerdigstillInngaaendeMedDokumentFil() {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertTrue(response.getBody().isJournalpostferdigstilt());
		assertNotNull(response.getBody().getDokumenter());
		assertNotNull(response.getBody().getDokumenter().getFirst().getDokumentInfoId());

		Journalpost journalpost = journalpostTestRepository.findById(Long.valueOf(response.getBody().getJournalpostId())).orElseThrow();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(J, journalpost.getJournalstatus());
		assertEquals("9999", journalpost.getJournalForendeEnhetId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertEquals(2, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.getFirst().getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.getFirst().getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.getFirst().getAksjon());
		assertThat(aksjonsLoggList.getFirst().getArkivElementEndringer()).hasSize(6);

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(1).getBruker());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());

		assertDokumentFilOpplastingArkiveres(DOKUMENT_FIL_ID_1, FYSISK_DOKUMENT);
		assertDokumentFilOpplastingArkiveres(DOKUMENT_FIL_ID_2, FYSISK_DOKUMENT_2);
		assertDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	@Test
	public void shouldFailWithConflictWhenDokumentFilIdReUsedWithSameEksternReferanseId() {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());
		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);
		assertEquals(CREATED, response.getStatusCode());
		assertDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);

		OpprettJournalpostRequest request2 = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());;
		HttpEntity<OpprettJournalpostRequest> requestEntity2 = new HttpEntity<>(request2, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response2 = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity2, OpprettJournalpostResponse.class);
		assertEquals(CONFLICT, response2.getStatusCode());
		assertThat(response2.getBody().getJournalpostId()).isEqualTo(response.getBody().getJournalpostId());
	}

	@Test
	public void shouldFailWithUnprocessableContentWhenDokumentFilIdReUsedWithDifferentEksternReferanseId() {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);
		assertEquals(CREATED, response.getStatusCode());
		assertDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);

		OpprettJournalpostRequest request2 = createBaseRequest(INNGAAENDE, SAK_ID.toString())
				.eksternReferanseId(UUID.randomUUID().toString())
				.dokumenter(createDokumenter())
				.build();
		HttpEntity<OpprettJournalpostRequest> requestEntity2 = new HttpEntity<>(request2, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response2 = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity2, ApplicationProblemDetail.class);
		assertEquals(UNPROCESSABLE_CONTENT, response2.getStatusCode());
		assertThat(response2.getBody().getDetail()).contains("Ugyldige dokumentFilId: "
				+ DOKUMENT_FIL_ID_1 + " (har tilstand=ARKIVERES, forventet LASTET_OPP), "
				+ DOKUMENT_FIL_ID_2 + " (har tilstand=ARKIVERES, forventet LASTET_OPP)");
		assertThat(journalpostTestRepository.findAll()).hasSize(1);
	}

	@ParameterizedTest
	@EnumSource(value = DokumentFilOpplastingTilstand.class, names = {"ARKIVERES", "ARKIVERT"})
	void shouldFailWithUnprocessableContentWhenDokumentFilIdAlleredeBrukt(DokumentFilOpplastingTilstand tilstand) {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT, tilstand);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, String.class);
		assertEquals(UNPROCESSABLE_CONTENT, response.getStatusCode());
		assertThat(response.getBody()).contains("Ugyldige dokumentFilId: " + DOKUMENT_FIL_ID_1 + " (har tilstand=" + tilstand + ", forventet LASTET_OPP)");
		assertThat(response.getBody()).doesNotContain(DOKUMENT_FIL_ID_2.toString());
		assertIngenSideeffekter(DOKUMENT_FIL_ID_2);
		assertNoDokumentFilIdHold(DOKUMENT_FIL_ID_1);
	}

	@Test
	void shouldFailWithUnprocessableContentWhenDokumentFilIdAlleredeBrukt() {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT, ARKIVERT);

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, String.class);
		assertEquals(UNPROCESSABLE_CONTENT, response.getStatusCode());
		assertThat(response.getBody()).contains("Ugyldige dokumentFilId: "
				+ DOKUMENT_FIL_ID_1 + " (har tilstand=ARKIVERT, forventet LASTET_OPP), "
				+ DOKUMENT_FIL_ID_2 + " (finnes ikke; finnes ikke i dokarkivmellomlager)");
		assertIngenSideeffekter();
		assertNoDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	@ParameterizedTest
	@EnumSource(value = DokumentFilOpplastingTilstand.class, names = {"LASTER_OPP", "FEILET"})
	void shouldFailWithBadRequestWhenDokumentFilIdHarFeilTilstand(DokumentFilOpplastingTilstand tilstand) {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT, tilstand);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, ApplicationProblemDetail.class);
		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody().getDetail()).contains("Kunne ikke opprette journalpost. Ugyldige dokumentFilId: " + DOKUMENT_FIL_ID_1 + " (har tilstand=" + tilstand + ", forventet LASTET_OPP)");
		assertThat(response.getBody().getDetail()).doesNotContain(DOKUMENT_FIL_ID_2.toString());
		assertIngenSideeffekter(DOKUMENT_FIL_ID_2);
		assertThat(dokumentFilOpplastingTestRepository.findById(DOKUMENT_FIL_ID_1).orElseThrow().getTilstand()).isEqualTo(tilstand);
		assertNoDokumentFilIdHold(DOKUMENT_FIL_ID_1);
	}

	@Test
	void shouldFailWithBadRequestWhenDuplikatDokumentFilId() {
		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_1));

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, ApplicationProblemDetail.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody().getDetail()).contains("dokumentFilId kan bare brukes i én dokumentvariant.");
		assertIngenSideeffekter();
		assertNoDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	@Test
	void shouldFailWithBadRequestWhenDokumentFilIdNotInDokumentFilOpplasting() {
		restStsToken();
		lastOppDokumentFil(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT);

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, ApplicationProblemDetail.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody().getDetail()).contains("Kunne ikke opprette journalpost. Ugyldige dokumentFilId: " + DOKUMENT_FIL_ID_2 + " (finnes ikke; finnes ikke i dokarkivmellomlager)");
		assertIngenSideeffekter(DOKUMENT_FIL_ID_1);
		assertNoDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	@Test
	void shouldFailWithBadRequestWhenDokumentFilIdNotInBucket() {
		restStsToken();

		dokumentFilOpplastingLastetOpp(DOKUMENT_FIL_ID_1, EKSTERN_DOKUMENT_REFERANSE_ID_1, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT);
		lastOppDokumentFil(DOKUMENT_FIL_ID_2, EKSTERN_DOKUMENT_REFERANSE_ID_2, APPLICATION_PDF_VALUE, FYSISK_DOKUMENT_2);

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", SAK_ID.toString(), createDokumenter());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<ApplicationProblemDetail> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, ApplicationProblemDetail.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody().getDetail()).contains("Kunne ikke opprette journalpost. Ugyldige dokumentFilId: " + DOKUMENT_FIL_ID_1 + " (finnes ikke i dokarkivmellomlager)");
		assertIngenSideeffekter(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
		assertNoDokumentFilIdHold(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	/// Ingen journalpost er opprettet og dokumentFilene med tilstand LASTET_OPP er urørt.
	private void assertIngenSideeffekter(UUID... lastetOppDokumentFilIder) {
		assertThat(journalpostTestRepository.findAll()).isEmpty();
		Arrays.stream(lastetOppDokumentFilIder).forEach(dokumentFilId ->
				assertThat(dokumentFilOpplastingTestRepository.findById(dokumentFilId).orElseThrow().getTilstand()).isEqualTo(LASTET_OPP));
		assertNoDokumentFilIdHold(lastetOppDokumentFilIder);
	}

	private void assertDokumentFilOpplastingArkiveres(UUID dokumentFilId, byte[] expectedFysiskDokument) {
		DokumentFilOpplasting dokumentFilOpplasting1 = dokumentFilOpplastingTestRepository.findById(dokumentFilId).orElseThrow();
		assertThat(dokumentFilOpplasting1.getCrc32cSjekksum()).isEqualTo(Crc32c.digest(expectedFysiskDokument));
		assertThat(dokumentFilOpplasting1.getTilstand()).isEqualTo(ARKIVERES);
	}

	private void lastOppDokumentFil(UUID uuid, String eksternDokumentReferanseId, String mediaType, byte[] fysiskDokument) {
		lastOppDokumentFil(uuid, eksternDokumentReferanseId, mediaType, fysiskDokument, LASTET_OPP);
	}

	private void lastOppDokumentFil(UUID uuid, String eksternDokumentReferanseId, String mediaType, byte[] fysiskDokument, DokumentFilOpplastingTilstand tilstand) {
		dokumentFilOpplastingLastetOpp(uuid, eksternDokumentReferanseId, mediaType, fysiskDokument, tilstand);
		dokarkivMellomlagerBucketStorage.uploadObject(uuid.toString(), new ByteArrayInputStream(fysiskDokument), mediaType);
	}

	private void dokumentFilOpplastingLastetOpp(UUID uuid, String eksternDokumentReferanseId, String mediaType, byte[] fysiskDokument) {
		dokumentFilOpplastingLastetOpp(uuid, eksternDokumentReferanseId, mediaType, fysiskDokument, LASTET_OPP);
	}

	private void dokumentFilOpplastingLastetOpp(UUID uuid, String eksternDokumentReferanseId, String mediaType, byte[] fysiskDokument, DokumentFilOpplastingTilstand tilstand) {
		DokumentFilOpplasting dokumentFilOpplasting = new DokumentFilOpplasting(
				uuid,
				eksternDokumentReferanseId,
				mediaType,
				sha256(fysiskDokument),
				"itest",
				LocalDateTime.now()
		);
		dokumentFilOpplasting.markerLastetOpp(fysiskDokument.length, Crc32c.digest(fysiskDokument), LocalDateTime.now(), "itest");
		dokumentFilOpplasting.setTilstand(tilstand);
		dokumentFilOpplastingTestRepository.persist(dokumentFilOpplasting);
		commitAndStartNewTransaction();
	}

	private static @NonNull List<Dokument> createDokumenter() {
		return createDokumenter(DOKUMENT_FIL_ID_1, DOKUMENT_FIL_ID_2);
	}

	private static @NonNull List<Dokument> createDokumenter(UUID dokumentFilId1, UUID dokumentFilId2) {
		return Arrays.asList(
				Dokument.builder()
						.tittel(DOKUMENT_TITTEL1)
						.brevkode(BREVKODE1)
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(Arrays.asList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.dokumentFilId(dokumentFilId1)
										.batchnavn(BATCHNAVN)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.filnavn(FILNAVN)
										.fysiskDokument(FYSISK_DOKUMENT_2)
										.batchnavn(BATCHNAVN)
										.build()))
						.build(),
				Dokument.builder()
						.tittel(DOKUMENT_TITTEL2)
						.brevkode(BREVKODE2)
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.rekkefoelge(2)
						.dokumentvarianter(Collections.singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(VARIANTFORMAT_ARKIV)
								.dokumentFilId(dokumentFilId2)
								.batchnavn(BATCHNAVN)
								.build()))
						.build());
	}

	private void assertDokumentFilIdHold(UUID... dokumentFilIds) {
		Arrays.stream(dokumentFilIds).forEach(uuid ->
				assertThat(dokarkivMellomlagerBucketStorage.isHeld(uuid.toString())).isTrue());
	}

	private void assertNoDokumentFilIdHold(UUID... dokumentFilIds) {
		Arrays.stream(dokumentFilIds).forEach(uuid ->
				assertThat(dokarkivMellomlagerBucketStorage.isHeld(uuid.toString())).isFalse());
	}
}