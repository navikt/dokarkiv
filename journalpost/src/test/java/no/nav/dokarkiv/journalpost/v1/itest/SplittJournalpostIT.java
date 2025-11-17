package no.nav.dokarkiv.journalpost.v1.itest;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest.SplittDokument;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.splittjournalpost.JournalpostSplitter.SPLITT_JOURNALPOST_FILNAVN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Slf4j
public class SplittJournalpostIT extends AbstractJournalpostIT {
	private static final String SPLITT_JOURNALPOST_PATH = JOURNALPOSTAPI_BASE_PATH + JOURNALPOSTAPI_JOURNALPOST_PATH + "/%s/splitt";
	private static final String NY_JOURNALPOST_TITTEL = "Ny Splittet Journalpost";
	private static final String NY_EKSTERN_REFERANSE_ID = UUID.randomUUID().toString();
	private static final String FEILMELDING = "Kunne ikke splitte journalpost med journalpostId=%s. Feilmelding=%s";

	@Test
	public void shouldSplittJournalpost() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.setJournalposttype(I);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var request = createRequest(journalpost);

		var requestHttpEntity = new HttpEntity<>(
				request,
				createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var response = restTemplate.exchange(SPLITT_JOURNALPOST_PATH.formatted(journalpost.getJournalpostId()), PATCH, requestHttpEntity, SplittJournalpostResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();

		TestTransaction.start();

		//Sjekk at opprinnelig journalpost er satt til Utgår (U)
		assertThat(journalpostTestRepository.findById(journalpost.getJournalpostId()))
				.isPresent()
				.get()
				.extracting(Journalpost::getJournalstatus)
				.isEqualTo(JournalStatusCode.U);

		assertThat(journalpostTestRepository.findById(response.getBody().nyJournalpostId()))
				.isPresent()
				.get()
				.satisfies( nyJournalpost -> {
					assertThat(nyJournalpost.getBehandlingstema()).isEqualTo(journalpost.getBehandlingstema());
					assertThat(nyJournalpost.getInnhold()).isEqualTo(NY_JOURNALPOST_TITTEL);
					assertThat(nyJournalpost.getJournalForendeEnhetId()).isEqualTo(journalpost.getJournalForendeEnhetId());
					assertThat(nyJournalpost.getKanalReferanseId()).isEqualTo(NY_EKSTERN_REFERANSE_ID);
					assertThat(nyJournalpost.getJournalstatus()).isEqualTo(M);

					assertThat(nyJournalpost.getBrukere())
							.extracting(no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerId, no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerType)
							.containsExactly(tuple(request.bruker().getId(), BrukerTypeCode.PERSON));

					assertThat(nyJournalpost.findAllDokumentInfos())
							.hasSameSizeAs(request.dokumenter())
							.satisfies(dokumentInfos -> {
								var dokumenterFraJournalpost = dokumentInfos.stream()
										.map(DokumentInfo::getDokumentInfoId)
										.toList();

								var dokumenterFraRequest = request.dokumenter().stream()
										.map(SplittDokument::dokumentInfoId)
										.toList();

								assertThat(dokumenterFraJournalpost).containsExactlyInAnyOrderElementsOf(dokumenterFraRequest);
							});
				});

		assertThat(aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpost.getJournalpostId()))
				.hasSize(3)
				.extracting(AksjonsLogg::getAksjon, AksjonsLogg::getJournalpostId, AksjonsLogg::getDokumentInfoId)
				.containsExactlyInAnyOrder(
						tuple(AksjonsTypeCode.SPLITT, journalpost.getJournalpostId(), null),
						tuple(AksjonsTypeCode.UTGAAR, journalpost.getJournalpostId(), null),
						tuple(AksjonsTypeCode.KOPIER_DOKUMENT, journalpost.getJournalpostId(), journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId()));
	}

	@Test
	public void shouldSplittJournalpostWithNewDokumenter() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.setJournalposttype(I);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var dokumentSomSkalKopieresUtenEndringer = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId();
		var dokumentSomSkalKopieresMedNyeVarianter = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(1).getDokumentInfoId();

		var dokumenter = List.of(
				new SplittDokument(dokumentSomSkalKopieresUtenEndringer, true, null),
				new SplittDokument(dokumentSomSkalKopieresMedNyeVarianter, false, List.of(createDokumentvariant()))
		);

		var request = createRequestWithDokumenter(journalpost, dokumenter);

		var requestHttpEntity = new HttpEntity<>(
				request,
				createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var response = restTemplate.exchange(SPLITT_JOURNALPOST_PATH.formatted(journalpost.getJournalpostId()), PATCH, requestHttpEntity, SplittJournalpostResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();

		TestTransaction.start();

		var nyJournalpost = journalpostTestRepository.findById(response.getBody().nyJournalpostId())
				.orElseThrow();

		// Finn det nye dokumentet som er opprettet for dokumentSomSkalKopieresMedNyeVarianter
		var nyttDokument = nyJournalpost.findAllDokumentInfos().stream()
				.filter(di -> !di.getDokumentInfoId().equals(dokumentSomSkalKopieresUtenEndringer))
				.findFirst()
				.orElseThrow();

		assertThat(nyttDokument.getTittel()).isEqualTo(journalpost.findDokumentInfoById(dokumentSomSkalKopieresMedNyeVarianter).getTittel());

		assertThat(nyJournalpost.findAllDokumentInfos())
				.hasSameSizeAs(request.dokumenter())
				.satisfies(dokumentInfos -> {
					var dokumenterFraNyJournapost = dokumentInfos.stream()
							.map(DokumentInfo::getDokumentInfoId)
							.toList();

					assertThat(dokumenterFraNyJournapost).contains(dokumentSomSkalKopieresUtenEndringer);
					assertThat(dokumenterFraNyJournapost).doesNotContain(dokumentSomSkalKopieresMedNyeVarianter);

					assertThat(nyttDokument)
							.satisfies(dokument -> {
								var varianterFraDokument = dokument.getFildetaljerListe();

								var varianterFraRequest = request.dokumenter().stream()
										.filter(d -> d.dokumentInfoId() == dokumentSomSkalKopieresMedNyeVarianter)
										.flatMap(d -> d.dokumentvarianter().stream())
										.findFirst()
										.orElseThrow();

								assertThat(varianterFraDokument)
										.extracting(FilDetaljer::getFiltype, FilDetaljer::getVariantFormat, FilDetaljer::getFilnavn)
										.containsExactly(tuple(
												FilTypeCode.valueOf(varianterFraRequest.getFiltype()),
												VariantFormatCode.valueOf(varianterFraRequest.getVariantformat()),
												SPLITT_JOURNALPOST_FILNAVN.formatted(
														dokumentSomSkalKopieresMedNyeVarianter,
														journalpost.getJournalpostId(),
														varianterFraRequest.getFiltype().toLowerCase())));

								assertThat(dokument)
										.satisfies(dokumentinfo ->
												assertThat(dokumentinfo.getFildetaljerListe())
														.allSatisfy(filDetaljer -> {
															var filFraDokument = dokumentFilTestRepository.findByFilUuid(filDetaljer.getFilUuid());
															var filFraRequest = varianterFraRequest.getFysiskDokument();

															assertThat(filFraDokument.getFil()).isEqualTo(filFraRequest);
														}));
							});
				});

		assertThat(aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpost.getJournalpostId()))
				.hasSize(4)
				.extracting(AksjonsLogg::getAksjon, AksjonsLogg::getJournalpostId, AksjonsLogg::getDokumentInfoId)
				.containsExactlyInAnyOrder(
						tuple(AksjonsTypeCode.SPLITT, journalpost.getJournalpostId(), null),
						tuple(AksjonsTypeCode.UTGAAR, journalpost.getJournalpostId(), null),
						tuple(AksjonsTypeCode.KOPIER_DOKUMENT, journalpost.getJournalpostId(), dokumentSomSkalKopieresUtenEndringer),
						tuple(AksjonsTypeCode.ENDRE_DOKUMENT, journalpost.getJournalpostId(), dokumentSomSkalKopieresMedNyeVarianter));

		var aksjonsloggNyJournalpost = aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(nyJournalpost.getJournalpostId());

		assertThat(aksjonsloggNyJournalpost)
				.singleElement()
				.extracting(AksjonsLogg::getAksjon, AksjonsLogg::getJournalpostId, AksjonsLogg::getDokumentInfoId, AksjonsLogg::getMelding)
				.containsExactly(AksjonsTypeCode.OPPRETT_FRA_SPLITT, response.getBody().nyJournalpostId(), null, "Journalposten ble splittet fra journalpostId=%s".formatted(journalpost.getJournalpostId()));

		String gamleBrukere = journalpost.getBrukere().stream().map(no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerId).collect(Collectors.joining(", "));
		String nyeBrukere = nyJournalpost.getBrukere().stream().map(no.nav.dokarkiv.core.domain.entities.Bruker::getBrukerId).collect(Collectors.joining(", "));

		assertThat(aksjonsloggNyJournalpost)
				.flatExtracting(AksjonsLogg::getArkivElementEndringer)
				.hasSize(6)
				.extracting(ArkivElementEndring::getArkivElement, ArkivElementEndring::getFraVerdi, ArkivElementEndring::getTilVerdi)
				.containsExactlyInAnyOrder(
						tuple("journalpost.fagomrade", journalpost.getFagomrade().name(), nyJournalpost.getFagomrade().name()),
						tuple("journalpost.innhold", journalpost.getInnhold(), nyJournalpost.getInnhold()),
						tuple("journalpost.avsend_mottaker", journalpost.getAvsenderMottaker(), nyJournalpost.getAvsenderMottaker()),
						tuple("journalpost.avsend_mottaker_id", journalpost.getAvsenderMottakerId(), nyJournalpost.getAvsenderMottakerId()),
						tuple("journalpost.journalf_enhet",journalpost.getJournalForendeEnhetId(), nyJournalpost.getJournalForendeEnhetId()),
						tuple("journalpost.bruker", gamleBrukere, nyeBrukere));
	}

	@Test
	void shouldReturnNotFoundWhenSplittingNonExistingJournalpost() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.setJournalposttype(I);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var requestHttpEntity = new HttpEntity<>(
				createRequest(journalpost),
				createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var journalpostIdSomIkkeFinnes = 123L;
		var response = restTemplate.exchange(SPLITT_JOURNALPOST_PATH.formatted(journalpostIdSomIkkeFinnes), PATCH, requestHttpEntity, ProblemDetail.class);

		assertThat(response)
				.isNotNull()
				.extracting(HttpEntity::getBody)
				.extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
				.containsExactly(NOT_FOUND.value(),
						FEILMELDING.formatted(123L, "Kunne ikke finne journalpost med journalpostId=%s i joark".formatted(journalpostIdSomIkkeFinnes)));
	}

	@Test
	void shouldReturnBadRequestWhenSplittingNonInngaaendeJournalpost() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var requestHttpEntity = new HttpEntity<>(
				createRequest(journalpost),
				createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var response = restTemplate.exchange(SPLITT_JOURNALPOST_PATH.formatted(journalpost.getJournalpostId()), PATCH, requestHttpEntity, ProblemDetail.class);

		assertThat(response)
				.isNotNull()
				.extracting(HttpEntity::getBody)
				.extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
				.containsExactly(BAD_REQUEST.value(), FEILMELDING.formatted(journalpost.getJournalpostId(), "Journalposten må være av type=I, men er av type=U"));
	}

	@Test
	void shouldReturnConflictWhenSplittingJournalpostWithExistingExternReferanseId() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.setJournalposttype(I);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var requestHttpEntity = new HttpEntity<>(
				createRequestWithExternReferanseId(journalpost, journalpost.getKanalReferanseId()),
				createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var response = restTemplate.exchange(SPLITT_JOURNALPOST_PATH.formatted(journalpost.getJournalpostId()), PATCH, requestHttpEntity, ProblemDetail.class);

		assertThat(response)
				.isNotNull()
				.extracting(HttpEntity::getBody)
				.extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
				.containsExactly(CONFLICT.value(), FEILMELDING.formatted(
						journalpost.getJournalpostId(), "eksternReferanseId=%s finnes allerede i joark".formatted(journalpost.getKanalReferanseId())));
	}

	@Test
	void shouldReturnBadRequestWhenRequestContainsNonExistingDocument() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpost.setJournalposttype(I);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var dokumentInfoIdSomIkkeFinnes = 123L;
		var dokumenter = List.of(new SplittDokument(dokumentInfoIdSomIkkeFinnes, true, null));

		var requestHttpEntity = new HttpEntity<>(
				createRequestWithDokumenter(journalpost, dokumenter),
				createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var response = restTemplate.exchange(SPLITT_JOURNALPOST_PATH.formatted(journalpost.getJournalpostId()), PATCH, requestHttpEntity, ProblemDetail.class);

		assertThat(response)
				.isNotNull()
				.extracting(HttpEntity::getBody)
				.extracting(ProblemDetail::getStatus, ProblemDetail::getDetail)
				.containsExactly(BAD_REQUEST.value(), FEILMELDING.formatted(
						journalpost.getJournalpostId(), "Dokument med dokumentInfoId=%s finnes ikke på den originale journalposten.".formatted(dokumentInfoIdSomIkkeFinnes)));
	}


	private static SplittJournalpostRequest createRequest(Journalpost journalpost) {
		return createRequestWithExternReferanseIdAndDokumenter(journalpost, NY_EKSTERN_REFERANSE_ID, createDokumenter(journalpost));
	}

	private static SplittJournalpostRequest createRequestWithExternReferanseId(Journalpost journalpost, String externReferanseId) {
		return createRequestWithExternReferanseIdAndDokumenter(journalpost, externReferanseId, createDokumenter(journalpost));
	}

	private static SplittJournalpostRequest createRequestWithDokumenter(Journalpost journalpost, List<SplittDokument> dokumenter) {
		return createRequestWithExternReferanseIdAndDokumenter(journalpost, NY_EKSTERN_REFERANSE_ID, dokumenter);
	}

	private static SplittJournalpostRequest createRequestWithExternReferanseIdAndDokumenter(Journalpost journalpost, String externReferanseId, List<SplittDokument> dokumenter) {
		return new SplittJournalpostRequest(
				journalpost.getFagomrade().name(),
				createBruker(),
				NY_JOURNALPOST_TITTEL,
				journalpost.getJournalForendeEnhetId(),
				externReferanseId,
				dokumenter);
	}

	private static Bruker createBruker() {
		return Bruker.builder()
				.id(BRUKER_ID)
				.idType(FNR)
				.build();
	}

	private static List<SplittDokument> createDokumenter(Journalpost journalpost) {
		return List.of(new SplittDokument(
				journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId(),
				true,
				null));
	}

	private static DokumentVariant createDokumentvariant() {
		return DokumentVariant.builder()
				.filtype(PDF.name())
				.variantformat(ARKIV.name())
				.fysiskDokument("abc".getBytes())
				.build();
	}
}
