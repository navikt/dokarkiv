package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SplittJournalpostValidatorTest {

	private static final String GYLDIG_EKSTERN_REFERANSE_ID = UUID.randomUUID().toString();
	private static final String GYLDIG_FNR = "12345678901";
	private static final String GYLDIG_ORGNR = "123456789";
	private static final String GYLDIG_TEMA = "DAG";
	private static final String GYLDIG_JOURNALFOERENDE_ENHET = "9999";
	private static final long GYLDIG_DOKUMENT_INFO_ID = 123456L;

	@Nested
	class ValidateHappyPath {

		@Test
		void shouldValidateRequest() {
			var request = createRequest().toBuilder()
					.tema(GYLDIG_TEMA)
					.bruker(createBruker())
					.tittel("Ny Splittet Journalpost")
					.journalfoerendeEnhet(GYLDIG_JOURNALFOERENDE_ENHET)
					.eksternReferanseId(GYLDIG_EKSTERN_REFERANSE_ID)
					.dokumenter(List.of(createSplittDokument()))
					.build();

			assertThatCode(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.doesNotThrowAnyException();
		}

		@Test
		void shouldValidateRequestWithMinimalFields() {
			assertThatCode(() -> SplittJournalpostValidator.valider(createRequest(), createJournalpost()))
					.doesNotThrowAnyException();
		}

		@Test
		void shouldValidateRequestWithMultipleDokumenter() {
			long dokumentInfoId1 = 111L;
			long dokumentInfoId2 = 222L;
			long dokumentInfoId3 = 333L;

			var journalpost = createJournalpost();

			Stream.of(dokumentInfoId1, dokumentInfoId2, dokumentInfoId3)
					.map(id -> {
						DokumentInfo dokumentInfo = createDokumentInfo().toBuilder()
								.dokumentInfoId(id)
								.build();
						dokumentInfo.setOriginalJournalpost(journalpost);
						return dokumentInfo;
					})
					.forEach(dokumentInfo -> {
						JournalpostDokumentInfoRelasjon relasjon =
								JournalpostDokumentInfoRelasjon.builder()
										.journalpost(journalpost)
										.dokumentInfo(dokumentInfo)
										.tilknyttetJournalpostSom(HOVEDDOKUMENT)
										.build();
						journalpost.addJournalpostDokumentInfoRelasjon(relasjon);
					});

			var request = createRequest().toBuilder()
					.dokumenter(List.of(
							createSplittDokument().toBuilder().dokumentInfoId(dokumentInfoId1).build(),
							createSplittDokument().toBuilder().dokumentInfoId(dokumentInfoId2).build(),
							createSplittDokument().toBuilder().dokumentInfoId(dokumentInfoId3).build()
					)).build();

			assertThatCode(() -> SplittJournalpostValidator.valider(request, journalpost))
					.doesNotThrowAnyException();
		}
	}

	@Nested
	class ValidateEksternReferanseId {

		@ParameterizedTest
		@NullAndEmptySource
		void shouldThrowExceptionWhenEksternReferanseIdIsNullOrEmpty(String eksternReferanseId) {
			var request = createRequest().toBuilder().eksternReferanseId(eksternReferanseId).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet eksternReferanseId kan ikke være null eller tomt");
		}

		@Test
		void shouldThrowExceptionWhenEksternReferanseIdIsTooLong() {
			var request = createRequest().toBuilder().eksternReferanseId("a".repeat(201)).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet eksternReferanseId kan ikke være over 200 tegn.");
		}

		@Test
		void shouldThrowExceptionWhenEksternReferanseIdContainsInvalidCharacters() {
			var request = createRequest().toBuilder().eksternReferanseId("###").build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet eksternReferanseId kan bare inneholde alfanumeriske tegn og følgende spesialtegn :;,.=-_~$&+*\"\\@!");
		}
	}

	@Nested
	class ValidateJournalfoerendeEnhet {
		@ParameterizedTest
		@ValueSource(strings = {"123", "12345", "999", "99999"})
		void shouldThrowExceptionWhenJournalfoerendeEnhetIsNotFourDigits(String invalidEnhet) {
			var request = createRequest().toBuilder().journalfoerendeEnhet(invalidEnhet).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet journalfoerendeEnhet må ha lengde=4, men har lengde=%s. journalfoerendeEnhet=%s",
							invalidEnhet.length(), invalidEnhet);
		}

		@ParameterizedTest
		@ValueSource(strings = {"abcd", "99a9"})
		void shouldThrowExceptionWhenJournalfoerendeEnhetContainsNonDigits(String invalidEnhet) {
			var request = createRequest().toBuilder().journalfoerendeEnhet(invalidEnhet).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet journalfoerendeEnhet må være et heltall. Mottatt verdi=%s. journalfoerendeEnhet=%s",
							invalidEnhet, invalidEnhet);
		}
	}

	@Nested
	class ValidateTema {

		@ParameterizedTest
		@MethodSource
		@NullAndEmptySource
		void shouldValidateTema(String validTema) {
			var request = createRequest().toBuilder().tema(validTema).build();

			assertThatCode(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.doesNotThrowAnyException();
		}

		static Stream<String> shouldValidateTema() {
			return Arrays.stream(FagomradeCode.values()).map(FagomradeCode::name);
		}

		@Test
		void shouldThrowExceptionWhenTemaIsInvalid() {
			var request = createRequest().toBuilder().tema("UGYLDIG").build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet tema=UGYLDIG er ikke et gyldig tema.");
		}

	}

	@Nested
	class ValidateDokumenter {

		@Test
		void shouldThrowExceptionWhenDokumenterIsEmpty() {
			var request = createRequest().toBuilder().dokumenter(List.of()).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumenter kan ikke være tomt.");
		}

		@Test
		void shouldThrowExceptionWhenDokumentDoesNotExistOnJournalpost() {
			long ikkeEksisterendeDokument = 111L;

			var request = createRequest().toBuilder()
					.dokumenter(List.of(createSplittDokument().toBuilder().dokumentInfoId(ikkeEksisterendeDokument).build()))
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Dokument med dokumentInfoId=%s finnes ikke på den originale journalposten.", ikkeEksisterendeDokument);
		}

		@Test
		void shouldThrowExceptionForMultipleDokumenterDoesNotExistOnJournalpost() {
			long ikkeEksisterendeDokument1 = 222L;
			long ikkeEksisterendeDokument2 = 333L;

			var request = createRequest().toBuilder()
					.dokumenter(List.of(
							createSplittDokument(), //Eksisterer
							createSplittDokument().toBuilder().dokumentInfoId(ikkeEksisterendeDokument1).build(),
							createSplittDokument().toBuilder().dokumentInfoId(ikkeEksisterendeDokument2).build()
					)).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Dokument med dokumentInfoId=%s finnes ikke på den originale journalposten.", ikkeEksisterendeDokument1)
					.withMessageContaining("Dokument med dokumentInfoId=%s finnes ikke på den originale journalposten.", ikkeEksisterendeDokument2);
		}
	}

	@Nested
	class ValidateSplittDokument {

		@ParameterizedTest
		@ValueSource(booleans = {true, false})
		void shouldValidateDokumentWithKopierUtenEndringer(boolean utenEndringer) {
			var request = createRequest().toBuilder()
					.dokumenter(List.of(createSplittDokument().toBuilder()
							.kopierUtenEndringer(utenEndringer)
							.dokumentvarianter(utenEndringer ? null : List.of(createDokumentVariant())).build()))
					.build();

			assertThatCode(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.doesNotThrowAnyException();
		}

		@Test
		void shouldValidateMultipleDokumentvarianter() {
			var variant1 = createDokumentVariant();
			var variant2 = createDokumentVariant().toBuilder().variantformat("ORIGINAL").build();
			var variant3 = createDokumentVariant().toBuilder().variantformat("SLADDET").build();

			var request = createRequest().toBuilder()
					.dokumenter(List.of(createSplittDokument().toBuilder()
							.dokumentvarianter(List.of(variant1, variant2, variant3)).build()))
					.build();

			assertThatCode(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.doesNotThrowAnyException();
		}

		@Test
		void shouldThrowExceptionWhenKopierUtenEndringerIsNull() {
			var request = createRequest().toBuilder()
					.dokumenter(List.of(createSplittDokument().toBuilder().kopierUtenEndringer(null).build()))
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokument.kopierUtenEndringer kan ikke være null.");
		}

		@Test
		void shouldThrowExceptionWhenKopierUtenEndringerIsTrueButDokumentvarianterIsNotEmpty() {
			var request = createRequest().toBuilder()
					.dokumenter(List.of(createSplittDokument().toBuilder().kopierUtenEndringer(true).build()))
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokument.dokumentvarianter skal kun oppgis dersom dokument.kopierUtenEndringer=false");
		}

		@Test
		void shouldCollectMultipleErrorsForSingleDokument() {
			long ikkeEksisterendeDokument = 111L;
			var dokument = createSplittDokument().toBuilder()
					.dokumentInfoId(ikkeEksisterendeDokument)
					.kopierUtenEndringer(null)
					.dokumentvarianter(List.of())
					.build();

			var request = createRequest().toBuilder().dokumenter(List.of(dokument)).build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokument.kopierUtenEndringer kan ikke være null")
					.withMessageContaining("Dokument med dokumentInfoId=%s finnes ikke på den originale journalposten", ikkeEksisterendeDokument);
		}
	}

	@Nested
	class ValidateDokumentvariant {

		@ParameterizedTest
		@NullAndEmptySource
		void shouldThrowExceptionWhenFiltypeIsNullOrEmpty(String filtype) {
			var variant = createDokumentVariant().toBuilder().filtype(filtype).build();
			var request = createRequestWithDokumentVariant(variant);

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumentvariant.filtype kan ikke være null eller tomt.");
		}

		@Test
		void shouldThrowExceptionWhenFiltypeIsInvalid() {
			var variant = createDokumentVariant().toBuilder().filtype("UGYLDIG").build();
			var request = createRequestWithDokumentVariant(variant);

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumentvariant.filtype er ikke gyldig. Mottatt filtype=UGYLDIG");
		}

		@ParameterizedTest
		@NullAndEmptySource
		void shouldThrowExceptionWhenVariantformatIsNullOrEmpty(String variantformat) {
			var variant = createDokumentVariant().toBuilder().variantformat(variantformat).build();
			var request = createRequestWithDokumentVariant(variant);

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumentvariant.variantformat kan ikke være null eller tomt.");
		}

		@Test
		void shouldThrowExceptionWhenVariantformatIsInvalid() {
			var variant = createDokumentVariant().toBuilder().variantformat("UGYLDIG").build();
			var request = createRequestWithDokumentVariant(variant);

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumentvariant.variantformat er ikke gyldig. Mottatt variantformat=UGYLDIG");
		}

		@ParameterizedTest
		@NullAndEmptySource
		void shouldThrowExceptionWhenFysiskDokumentIsNullOrEmpty(byte[] fysiskDokument) {
			var variant = createDokumentVariant().toBuilder().fysiskDokument(fysiskDokument).build();
			var request = createRequestWithDokumentVariant(variant);

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumentvariant.fysiskDokument kan ikke være null eller tomt.");
		}

		@Test
		void shouldCollectMultipleErrorsForDokumentvariant() {
			var variant = createDokumentVariant().toBuilder()
					.filtype(null)
					.variantformat(null)
					.fysiskDokument(null)
					.build();
			var request = createRequestWithDokumentVariant(variant);

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet dokumentvariant.filtype kan ikke være null eller tomt.")
					.withMessageContaining("Feltet dokumentvariant.variantformat kan ikke være null eller tomt.")
					.withMessageContaining("Feltet dokumentvariant.fysiskDokument kan ikke være null eller tomt.");
		}
	}

	@Nested
	class ValidateBruker {

		@ParameterizedTest
		@MethodSource
		@NullSource
		void shouldValidateBruker(Bruker bruker) {
			var request = createRequest().toBuilder()
					.bruker(bruker)
					.build();

			assertThatCode(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.doesNotThrowAnyException();
		}

		static Stream<Bruker> shouldValidateBruker() {
			return Stream.of(
					createBruker(), //FNR
					Bruker.builder().id(GYLDIG_ORGNR).idType(ORGNR).build()
			);
		}

		@ParameterizedTest
		@NullAndEmptySource
		void shouldThrowExceptionWhenBrukerIdIsNullOrEmpty(String brukerId) {
			var request = createRequest().toBuilder()
					.bruker(createBruker().toBuilder().id(brukerId).build())
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet bruker.id må være satt");
		}

		@ParameterizedTest
		@ValueSource(strings = {"1234567890", "123456789012", "abcdefghijk"})
		void shouldThrowExceptionWhenFnrIsNotElevenDigits(String invalidFnr) {
			var request = createRequest().toBuilder()
					.bruker(createBruker().toBuilder().id(invalidFnr).build())
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet bruker.id må være 11 siffer dersom bruker.idType=%s", FNR.name());
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345678", "1234567890", "abcdefghi"})
		void shouldThrowExceptionWhenOrgnrIsNotNineDigits(String invalidOrgnr) {
			var request = createRequest().toBuilder()
					.bruker(createBruker().toBuilder().id(invalidOrgnr).idType(ORGNR).build())
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet bruker.id må være 9 siffer dersom bruker.idType=%s", ORGNR.name());
		}

		@Test
		void shouldThrowExceptionWhenBrukerIdTypeIsNotSupported() {
			var request = createRequest().toBuilder()
					.bruker(createBruker().toBuilder().idType(BrukerIdType.AKTOERID).build())
					.build();

			assertThatExceptionOfType(InputValideringFeiletException.class)
					.isThrownBy(() -> SplittJournalpostValidator.valider(request, createJournalpost()))
					.withMessageContaining("Feltet bruker.idType må være en av %s", List.of(FNR.name(), ORGNR.name()));
		}
	}

	private static SplittJournalpostRequest createRequest() {
		return SplittJournalpostRequest.builder()
				.eksternReferanseId(GYLDIG_EKSTERN_REFERANSE_ID)
				.dokumenter(List.of(createSplittDokument()))
				.build();
	}

	private static SplittJournalpostRequest createRequestWithDokumentVariant(DokumentVariant dokumentVariant) {
		return createRequest().toBuilder()
				.dokumenter(List.of(createSplittDokumentWithDokumentvariant(dokumentVariant)))
				.build();
	}

	private static SplittJournalpostRequest.SplittDokument createSplittDokumentWithDokumentvariant(DokumentVariant dokumentvarianter) {
		return createSplittDokument().toBuilder()
				.dokumentvarianter(List.of(dokumentvarianter))
				.build();
	}

	private static SplittJournalpostRequest.SplittDokument createSplittDokument() {
		return SplittJournalpostRequest.SplittDokument.builder()
				.dokumentInfoId(GYLDIG_DOKUMENT_INFO_ID)
				.kopierUtenEndringer(false)
				.dokumentvarianter(List.of(createDokumentVariant()))
				.build();
	}
	
	private static DokumentVariant createDokumentVariant() {
		return DokumentVariant.builder()
				.filtype(PDF.name())
				.variantformat(ARKIV.name())
				.fysiskDokument("abc".getBytes())
				.build();
	}

	private static Bruker createBruker() {
		return Bruker.builder()
				.idType(FNR)
				.id(GYLDIG_FNR)
				.build();
	}

	private static DokumentInfo createDokumentInfo() {
		return DokumentInfo.builder()
				.dokumentInfoId(GYLDIG_DOKUMENT_INFO_ID)
				.tittel("Dokument-tittel")
				.build();
	}
	
	private static Journalpost createJournalpost() {
		var journalpost = Journalpost.builder()
				.journalpostId(1L)
				.journalposttype(I)
				.fagomrade(FagomradeCode.DAG)
				.innhold("Journalpost-tittel")
				.build();

		var dokument = createDokumentInfo();
		dokument.setOriginalJournalpost(journalpost);

		var relasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokument)
				.tilknyttetJournalpostSom(HOVEDDOKUMENT)
				.build();

		journalpost.addJournalpostDokumentInfoRelasjon(relasjon);

		return journalpost;
	}
}

