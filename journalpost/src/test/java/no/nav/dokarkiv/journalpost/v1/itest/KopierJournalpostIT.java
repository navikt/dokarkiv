package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_JOURNALPOST;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.entities.Journalpost.KANAL_REFERANSE_ID_LENGTH;
import static no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.TestdataFactory.createJournalpostWithHoveddokumentAndVedlegg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class KopierJournalpostIT extends AbstractJournalpostIT {
	private static final String BAD_REQUEST_FEILMELDING = "Kan ikke kopiere journalpost med journalpostId=%s fordi journalpost har ugyldig status=%s";
	private static final String EKSTERN_REFERANSE_ID = "fagsystemet-sin-referanse";

	@ParameterizedTest
	@MethodSource("journalpostTypeMedGyldigJournalpostStatus")
	public void shouldHappyKopierJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus, JournalStatusCode kopierJournalStatus) {
		restStsToken();
		final String eksternReferanseId = UUID.randomUUID().toString();

		Journalpost journalpost = createJournalpostWithHoveddokumentAndVedlegg(journalpostType, journalStatus);
		Journalpost originalJournalpost = buildAndCommit(journalpost);

		RequestEntity<KopierJournalpostRequest> kopierRequestEntity = RequestEntity.post(URL_JOURNALPOST + KOPIER_QUERY,
						originalJournalpost.getJournalpostId())
				.headers(createHeadersWithServiceUserToken())
				.body(KopierJournalpostRequest.builder().eksternReferanseId(eksternReferanseId).build());

		ResponseEntity<KopierJournalpostResponse> journalpostResponse = restTemplate.exchange(kopierRequestEntity, KopierJournalpostResponse.class);
		KopierJournalpostResponse kopierJournalpostResponse = journalpostResponse.getBody();

		TestTransaction.start();

		Journalpost kopiertJournalpost = journalpostTestRepository.findById(valueOf(kopierJournalpostResponse.getKopierJournalpostId()))
				.orElseThrow(RuntimeException::new);

		assertThat(journalpostResponse.getStatusCode()).isEqualTo(CREATED);
		assertThat(journalpostResponse.getBody()).isNotNull();

		assertThat(kopiertJournalpost.getAvsenderMottakerIdType()).isEqualTo(originalJournalpost.getAvsenderMottakerIdType());
		assertThat(kopiertJournalpost.getAvsenderMottakerId()).isEqualTo(originalJournalpost.getAvsenderMottakerId());
		assertThat(kopiertJournalpost.getAvsenderMottaker()).isEqualTo(originalJournalpost.getAvsenderMottaker());
		assertThat(kopiertJournalpost.getLand()).isEqualTo(originalJournalpost.getLand());

		assertThat(kopiertJournalpost.getJournalposttype()).isEqualTo(originalJournalpost.getJournalposttype());
		assertThat(kopiertJournalpost.getJournalstatus()).isEqualTo(kopierJournalStatus);

		assertThat(kopiertJournalpost.getChangeStamp().getCreatedDate()).isAfter(originalJournalpost.getChangeStamp().getCreatedDate());
		assertThat(kopiertJournalpost.getChangeStamp().getCreatedBy()).isEqualTo(SERVICE_USER_ID);

		assertThat(kopiertJournalpost.getUtsendingskanal()).isEqualTo(originalJournalpost.getUtsendingskanal());
		assertThat(kopiertJournalpost.getInnhold()).isEqualTo(originalJournalpost.getInnhold());
		assertThat(kopiertJournalpost.getBehandlingstema()).isEqualTo(originalJournalpost.getBehandlingstema());
		assertThat(kopiertJournalpost.getFagomrade()).isEqualTo(originalJournalpost.getFagomrade());
		assertThat(kopiertJournalpost.getMottakskanal()).isEqualTo(originalJournalpost.getMottakskanal());
		assertThat(kopiertJournalpost.getAntallRetur()).isEqualTo(originalJournalpost.getAntallRetur());
		assertThat(kopiertJournalpost.getKanalReferanseId()).isEqualTo(eksternReferanseId);
		assertThat(kopiertJournalpost.getJournalForendeEnhetId()).isEqualTo(originalJournalpost.getJournalForendeEnhetId());
		assertThat(kopiertJournalpost.getJournalfortAvNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(kopiertJournalpost.getInnsyn()).isEqualTo(originalJournalpost.getInnsyn());
		assertThat(kopiertJournalpost.getSkjermingType()).isEqualTo(originalJournalpost.getSkjermingType());

		assertThat(kopiertJournalpost.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(kopiertJournalpost.getMottattDato()).isInSameDayAs(originalJournalpost.getMottattDato());
		assertThat(kopiertJournalpost.getSendtPrintDato()).isInSameHourWindowAs(originalJournalpost.getSendtPrintDato());
		assertThat(kopiertJournalpost.getAvsendtReturDato()).isInSameDayAs(originalJournalpost.getAvsendtReturDato());

		kopiertJournalpost.getJournalpostDokumentInfoRelasjoner().forEach(jpdok -> {
			assertThat(jpdok.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
			assertThat(jpdok.getEndretKildeNavn()).isEqualTo(SERVICE_USER_ID);
		});
		assertThat(kopiertJournalpost.getBrukere().size()).isEqualTo(originalJournalpost.getBrukere().size());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		aksjonsLoggList.forEach(aksjonsLogg -> {
			assertThat(aksjonsLogg.getUtfoertAv()).isEqualTo(SERVICE_USER_ID);
			assertThat(aksjonsLogg.getJournalpostId()).isEqualTo(originalJournalpost.getJournalpostId());
			assertThat(aksjonsLogg.getAksjon()).isEqualTo(KOPIER_JOURNALPOST);
			aksjonsLogg.getArkivElementEndringer().forEach(arkivElementEndring -> {
				assertThat(arkivElementEndring.getFraVerdi()).isEqualTo(valueOf(originalJournalpost.getJournalpostId()));
				assertThat(arkivElementEndring.getTilVerdi()).isEqualTo(valueOf(kopiertJournalpost.getJournalpostId()));
			});
		});

		kopiertJournalpost.getTilleggsopplysninger().forEach(
				(key, value) -> assertThat(value).isEqualTo(originalJournalpost.getTilleggsopplysninger().get(key))
		);

		List<DokumentInfo> kopierDokumentInfos = kopiertJournalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.toList();

		List<DokumentInfo> originalDokumentInfo = originalJournalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.toList();

		kopierDokumentInfos.forEach(kopierDokumentInfo -> {
			AtomicInteger atomicInteger = new AtomicInteger();
			DokumentInfo orgDokumentInfo = originalDokumentInfo.get(atomicInteger.get());
			assertThat(kopierDokumentInfo.getDokumentInfoId()).isEqualTo(orgDokumentInfo.getDokumentInfoId());
			assertThat(kopierDokumentInfo.getDokumentstatus()).isEqualTo(orgDokumentInfo.getDokumentstatus());
			assertThat(kopierDokumentInfo.getBrevkode()).isEqualTo(orgDokumentInfo.getBrevkode());
			assertThat(kopierDokumentInfo.getBrevgruppe()).isEqualTo(orgDokumentInfo.getBrevgruppe());
			assertThat(kopierDokumentInfo.getKassert()).isEqualTo(orgDokumentInfo.getKassert());
			assertThat(kopierDokumentInfo.getTittel()).isEqualTo(orgDokumentInfo.getTittel());
			assertThat(kopierDokumentInfo.getDatoKassert()).isEqualTo(orgDokumentInfo.getDatoKassert());
			assertThat(kopierDokumentInfo.getKategori()).isEqualTo(orgDokumentInfo.getKategori());
			assertThat(kopierDokumentInfo.getSensitivt()).isEqualTo(orgDokumentInfo.getSensitivt());
			assertThat(kopierDokumentInfo.getDokumenttypeId()).isEqualTo(orgDokumentInfo.getDokumenttypeId());

			assertThat(kopierDokumentInfo.getFildetaljerListe().size()).isEqualTo(orgDokumentInfo.getFildetaljerListe().size());
			FilDetaljer originalFilDetaljer = orgDokumentInfo.getFildetaljerListe().iterator().next();
			kopierDokumentInfo.getFildetaljerListe().forEach(fd -> {
				assertThat(fd.getId()).isEqualTo(originalFilDetaljer.getId());
				assertThat(fd.getFilnavn()).isEqualTo(originalFilDetaljer.getFilnavn());
				assertThat(fd.getFilUuid()).isEqualTo(originalFilDetaljer.getFilUuid());
				assertThat(fd.getBatchNavn()).isEqualTo(originalFilDetaljer.getBatchNavn());
				assertThat(fd.getFilstorrelse()).isEqualTo(originalFilDetaljer.getFilstorrelse());
				assertThat(fd.getVariantFormat()).isEqualTo(originalFilDetaljer.getVariantFormat());
				assertThat(fd.getFildetaljerId()).isEqualTo(originalFilDetaljer.getFildetaljerId());
				assertThat(fd.getFiltype()).isEqualTo(originalFilDetaljer.getFiltype());
				assertThat(fd.getChangeStamp().getCreatedDate().toInstant()).isEqualTo(originalFilDetaljer.getChangeStamp().getCreatedDate().toInstant());
			});


			Bruker originalBruker = originalJournalpost.getBrukere().iterator().next();

			kopiertJournalpost.getBrukere().forEach(kopierJp -> {
				assertThat(kopierJp.getBrukerId()).isEqualTo(originalBruker.getBrukerId());
				assertThat(kopierJp.getBrukerType()).isEqualTo(originalBruker.getBrukerType());
				assertThat(kopierJp.getChangeStamp().getCreatedDate()).isAfter(originalBruker.getChangeStamp().getCreatedDate());
			});

			assertThat(kopiertJournalpost.getSaksrelasjon()).isNull();
		});
	}

	private static Stream<Arguments> journalpostTypeMedGyldigJournalpostStatus() {
		return Stream.of(Arguments.of(I, J, M),
				Arguments.of(U, FS, D),
				Arguments.of(U, FL, D),
				Arguments.of(U, E, D),
				Arguments.of(N, FS, D));
	}

	@ParameterizedTest
	@MethodSource("journalpostTypeMedUgyldigJournalpostStatus")
	public void shouldThrowBadRequestExceptionWhenJournalpostHaveInvalidStatus(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		restStsToken();

		Journalpost journalpost = createJournalpostWithHoveddokumentAndVedlegg(journalpostType, journalStatus);
		Journalpost originalJournalpost = buildAndCommit(journalpost);

		RequestEntity<KopierJournalpostRequest> kopierRequestEntity = RequestEntity.post(URL_JOURNALPOST + KOPIER_QUERY,
						originalJournalpost.getJournalpostId())
				.headers(createHeadersWithServiceUserToken())
				.body(KopierJournalpostRequest.builder().eksternReferanseId(EKSTERN_REFERANSE_ID).build());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(kopierJournalpostResponse.getBody()).contains(format(BAD_REQUEST_FEILMELDING, originalJournalpost.getJournalpostId(), originalJournalpost.getJournalstatus()));
	}

	private static Stream<Arguments> journalpostTypeMedUgyldigJournalpostStatus() {
		return Stream.of(Arguments.of(I, R),
				Arguments.of(I, A),
				Arguments.of(U, M),
				Arguments.of(U, A),
				Arguments.of(U, MO),
				Arguments.of(U, OD),
				Arguments.of(N, M));
	}

	@Test
	public void shouldThrowNotFoundWhenJournalpostNotFoundInJoark() {
		restStsToken();

		RequestEntity<KopierJournalpostRequest> kopierRequestEntity = RequestEntity.post(URL_JOURNALPOST + KOPIER_QUERY,
						"123")
				.headers(createHeadersWithServiceUserToken())
				.body(KopierJournalpostRequest.builder().eksternReferanseId(EKSTERN_REFERANSE_ID).build());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(kopierJournalpostResponse.getBody()).contains("Kunne ikke finne journalpost med journalpostId=123 i joark");
	}

	@ParameterizedTest
	@MethodSource("ugyldigJournalpostIdArguments")
	public void shouldThrowBadRequestWhenJournalpostIdAreNullOrNonNumeric(String journalpostId, String message) {
		stubAzure();
		restStsToken();

		RequestEntity<KopierJournalpostRequest> kopierRequestEntity = RequestEntity.post(URL_JOURNALPOST + KOPIER_QUERY,
						journalpostId)
				.headers(createHeadersWithServiceUserToken())
				.body(KopierJournalpostRequest.builder().eksternReferanseId(EKSTERN_REFERANSE_ID).build());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(kopierJournalpostResponse.getBody()).contains(message);
	}

	private static Stream<Arguments> ugyldigJournalpostIdArguments() {
		return Stream.of(
				Arguments.of("", "kildeJournalpostId kan ikke være null eller tomt. kildeJournalpostId="),
				Arguments.of(" ", "kildeJournalpostId kan ikke være null eller tomt. kildeJournalpostId="),
				Arguments.of("NAV", "kildeJournalpostId må være et heltall. Mottatt verdi=NAV. kildeJournalpostId=NAV")
		);
	}

	@ParameterizedTest
	@MethodSource("ugyldigEksternReferanseIdArguments")
	public void shouldThrowBadRequestWhenEksternReferanseIdUgyldig(String eksternReferanseId, String message) {
		stubAzure();
		restStsToken();

		RequestEntity<KopierJournalpostRequest> kopierRequestEntity = RequestEntity.post(URL_JOURNALPOST + KOPIER_QUERY,
						"123")
				.headers(createHeadersWithServiceUserToken())
				.body(KopierJournalpostRequest.builder().eksternReferanseId(eksternReferanseId).build());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(kopierJournalpostResponse.getBody()).contains(message);
	}

	private static Stream<Arguments> ugyldigEksternReferanseIdArguments() {
		return Stream.of(
				Arguments.of("", "eksternReferanseId kan ikke være null eller tomt"),
				Arguments.of(" ", "eksternReferanseId kan ikke være null eller tomt"),
				Arguments.of("a".repeat(KANAL_REFERANSE_ID_LENGTH + 1), "eksternReferanseId kan ikke være over 200 tegn. Mottatt eksternReferanseId=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
		);
	}

	@Test
	public void shouldThrowConflictWhenDuplicateEksternReferanseId() {
		restStsToken();

		Journalpost journalpost = createJournalpostWithHoveddokumentAndVedlegg(I, J);
		Journalpost originalJournalpost = buildAndCommit(journalpost);

		RequestEntity<KopierJournalpostRequest> kopierRequestEntity = RequestEntity.post(URL_JOURNALPOST + KOPIER_QUERY,
						originalJournalpost.getJournalpostId())
				.headers(createHeadersWithServiceUserToken())
				.body(KopierJournalpostRequest.builder().eksternReferanseId(EKSTERN_REFERANSE_ID).build());

		ResponseEntity<KopierJournalpostResponse> journalpostResponse = restTemplate.exchange(kopierRequestEntity, KopierJournalpostResponse.class);
		assertThat(journalpostResponse.getStatusCode()).isEqualTo(CREATED);
		TestTransaction.start();
		commitAndStartNewTransaction();

		ResponseEntity<String> journalpostResponseConflict = restTemplate.exchange(kopierRequestEntity, String.class);
		assertThat(journalpostResponseConflict.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(journalpostResponseConflict.getBody()).contains(journalpostResponse.getBody().getKopierJournalpostId());
	}
}
