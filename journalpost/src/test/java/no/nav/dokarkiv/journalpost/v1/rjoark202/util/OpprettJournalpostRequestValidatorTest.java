package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestUtils;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.LOVLIGE_INNSYNSKODER;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.MASKINELL_JOURNALFOERENDE_ENHET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

public class OpprettJournalpostRequestValidatorTest {

	public static final String FORSOEKFERDIGSTILL = "false";

	private final OpprettJournalpostRequestValidator validator = new OpprettJournalpostRequestValidator();

	@Test
	public void happyPath() {
		OpprettJournalpostRequest request = createRequest(JournalpostType.INNGAAENDE);

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldNotThrowExceptionIfMottakskanalTemaCombinationIsValid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SER)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldValidateWhenNoAvsenderMottaker() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(null)
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathGenerellSakTemaUFO() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@ParameterizedTest
	@EnumSource(value = JournalpostType.class, names = {"INNGAAENDE"}, mode = EXCLUDE)
	public void shouldJournalfoereWhenJournalfoerendeEnhetEr9999AndJournpostTypeErUlikInngaaendeAndForsoekFerdigstillErTrue(JournalpostType journalpostType) {
		OpprettJournalpostRequest request = createMinimalRequest(journalpostType)
				.journalfoerendeEnhet(TestUtils.JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldValidateOkWhenJournaforendeEnhetErNull() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(null)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsNotNullOrNot4Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET_UGYLDIG)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains(
				"Journalpost.journalfoerendeEnhet må være null eller fire siffer. Mottatt journalfoerendeEnhet=" + JOURNALFOERENDE_ENHET_UGYLDIG);
	}


	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsLotsOfSpaces() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains(
				"Journalpost.journalfoerendeEnhet må være null eller fire siffer. Mottatt journalfoerendeEnhet=" + TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES);
	}

	@Test
	public void happyPathGenerellSakTemaPEN() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenTemaNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(null)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("tema");

	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdNotSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(null).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemNotSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(null).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).arkivsaksnummer(ARKIVSAKSNUMMER).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).arkivsaksystem(Arkivsaksystem.GSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).fagsakId(FAGSAK_ID).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).fagsaksystem(Fagsaksystem.AO01).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksnummerNotSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenJournalfoerendeEnhetEr9999AndJournalpostTypeErInngaaendeAndForsoekFerdigstillErFalse() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(MASKINELL_JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Ikke mulig å opprette journalpost med type inngaaende på journalfoerendeEnhet=9999 (maskinell) så lenge journalposten ikke forsøkes å ferdigstilles");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemNotSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdIsSetButNotIdType() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("11223344556")
						.idType(null)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.idType");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeIsSetAndNotId() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(null)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndIdNot11Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("1111111111a")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndMoreThan11Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("111111111111")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdNot9Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("NO7777777")
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"88888888", "1010101010"})
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdLessThan9OrMoreThan9Digits(String orgnr) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(orgnr)
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"7777777", "88888888", "999999999"})
	public void shouldValidateWhenAvsenderMottakerIdTypeHPRNRAnd7To9Digits(String hprnr) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(hprnr)
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdNotANumber() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("777777a")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"666666", "1010101010"})
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdLessThan7OrMoreThan9Digits(String hprnr) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(hprnr)
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingId() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.id(null)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc11111111")
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("1122334455")
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.ORGNR)
						.id("1122334455")
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForAktoerid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.AKTOERID)
						.id("1122334455")
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfTemaIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema("tema")
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("tema");
	}

	@Test
	public void shouldThrowExceptionIfBehandlingstemaIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("behandlingstema")
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("behandlingstema");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeKanalIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.kanal("kanal")
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("kanal");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeAndMottaksKanalIsNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.kanal(null)
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Kanal er påkrevd for inngående journalposter");
	}

	@Test
	public void shouldThrowExceptionIfMottakskanalTemaCombinationIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Det er kun mulig å arkivere med mottakskanal=NAV_NO_UINNLOGGET dersom tema=SER");
	}

	@Test
	public void shouldThrowExceptionIfUtgaaendeKanalIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.kanal("kanal")
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("kanal");
	}

	@Test
	public void shouldThrowExceptionIfSakIsMissingArkivsaksnummer() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(null)
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionIfArkivsaksnummerNotNumeric() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer("quack123")
						.build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.arkivsaksnummer");
	}

 	@Test
	public void shouldThrowExceptionIfDokumentkategoriIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori("kategori")
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentkategori validerer ikke mot kodeverk. Gyldige verdier for dokumentkategori er %s. Mottatt dokumentkategori=kategori"
				.formatted(Arrays.toString(DokumentKategoriCode.values())));
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(null)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant(ARKIV).filtype må være satt");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype("filtype")
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant(ARKIV).filtype validerer ikke mot kodeverk. Gyldige verdier for filtype er");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalidForARKIV() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_XML)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant(ARKIV).filtype må være PDF eller PDFA for Dokument.dokumentvariant.variantformat=ARKIV");
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(null)
								.build()))
						.build()))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant.variantformat må være satt");
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat("variantformat")
								.build()))
						.build()))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant(variantformat).variantformat validerer ikke mot kodeverk. Gyldige verdier for variantformat er");
	}

	@Test
	public void shouldThrowExceptionIfDokumenterIsEmpty() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(new ArrayList<>())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("dokumenter");
	}

	@Test
	public void shouldThrowExceptionWhenBehandlingstemaIsNotValid() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("ab333")
				.avsenderMottaker(null)
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Behandlingstema må være på formatet ´ab + 4 siffer´. Mottatt behandlingstema=ab333");
	}

	@Test
	public void shouldNotThrowExceptionIfDifferentVariantformat() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build()))
						.build()))
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenDocumentHasDuplicateVariantformat() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Dokumenter.dokumentvariant.variantformat");
	}

	@Test
	public void shouldThrowExceptionWhenVariantformatArkivIsMissing() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(
								DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Alle dokumenter må innholde en dokumentvariant av typen ARKIV");
	}

	@Test
	public void shouldThrowExceptionWhenDocumentHasNoVariantformat() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Alle dokumenter må innholde en dokumentvariant av typen ARKIV");
	}

	@Test
	public void shouldThrowExceptionWhenADocumentHasMultipleVariantformatArkiv() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Variantformat=ARKIV funnet 2 ganger");
	}

	@Test
	public void shoudThrowExeceptionIfNotAtleastOneDocumentIsPresent() {
		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
				.journalposttype(INNGAAENDE)
				.tema(FagomradeCode.FOR.name())
				.kanal("NAV_NO")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Kan ikke opprette journalpost uten dokumenter");
	}

	@ParameterizedTest
	@ValueSource(strings = {"alfanumeriskString1", "65efa501-1554-4538-a553-1db5b31ad40b", "StrengMed\\backslash", "epost@adresse.noe", "AlleGyldigeTegn2:;,.=-_~$&+*\"\\@!"})
	void validEksternReferanseId(String eksternReferanseId) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.eksternReferanseId(eksternReferanseId)
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@ParameterizedTest
	@MethodSource("feilEksternReferanseId")
	void shouldThrowExceptionWhenEksternReferanseIdIsMalformed(String eksternReferanseId, String forventetFeilmelding) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.eksternReferanseId(eksternReferanseId)
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				forventetFeilmelding);
	}

	private static Stream<Arguments> feilEksternReferanseId() {
		return Stream.of(
				Arguments.of("bj5bzAng3tvvY7ao0A15Kj8lq3RuN78rPTDYQp9lz416At7egwxVKw3klqZngX39eYdwqDIs6KUbGurS97R78Mz25WO3r7ththg8QVf2HY1col7713VLSSFHvQKHzftl2aKIXF48pnftmwbNX201aX2msQDb8G8nd31gyzfvzZvYX0hcPeU9g5nm5NeV43RLRaKyR1BLG",
						"EksternReferanseId kan ikke være over 200 tegn. Mottatt eksternReferanseId=bj5bzAng3tvvY7ao0A15Kj8lq3RuN78rPTDYQp9lz416At7egwxVKw3klqZngX39eYdwqDIs6KUbGurS97R78Mz25WO3r7ththg8QVf2HY1col7713VLSSFHvQKHzftl2aKIXF48pnftmwbNX201aX2msQDb8G8nd31gyzfvzZvYX0hcPeU9g5nm5NeV43RLRaKyR1BLG"),
				Arguments.of("ØÆÅhører og mellomrom hører ikke hjemme i url og dermed i eksternReferanseId",
						"EksternReferanseId kan bare inneholde alfanumeriske tegn og følgende spesialtegn :;,.=-_~$&+*\"\\@! Mottatt eksternReferanseId=ØÆÅhører og mellomrom hører ikke hjemme i url og dermed i eksternReferanseId")
		);
	}

	@Test
	void shouldThrowExceptionWhenDatoIsInTheFuture() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("ab0001")
				.avsenderMottaker(null)
				.datoDokument(LocalDateTime.now().plusDays(3))
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains(format("Validering av %s feilet. Dato kan ikke være frem i tid.", "DatoDokument"));
	}

	@ParameterizedTest
	@MethodSource
	void shouldLogWarningWhenDatoMottattIsAfter(Date innsendtDato) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("ab0001")
				.avsenderMottaker(null)
				.datoMottatt(innsendtDato)
				.build();

		assertDoesNotThrow(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
	}

	private static Stream<Arguments> shouldLogWarningWhenDatoMottattIsAfter() {
		var naatid = LocalDate.now();

		return Stream.of(
				Arguments.of(Date.from(naatid.atStartOfDay().atZone(ZoneId.of("Z")).toInstant().plus(23, HOURS).plus(59, MINUTES)))
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldNotLogWarningWhenDatoMottattIsBeforeOrSameDate(Date innsendtDato) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("ab0001")
				.avsenderMottaker(null)
				.datoMottatt(innsendtDato)
				.build();

		assertDoesNotThrow(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
	}

	private static Stream<Arguments> shouldNotLogWarningWhenDatoMottattIsBeforeOrSameDate() {
		var naatid = LocalDate.now();

		return Stream.of(
				Arguments.of(Date.from(naatid.atStartOfDay().atZone(systemDefault()).toInstant())),
				Arguments.of(Date.from(naatid.atStartOfDay().atZone(systemDefault()).toInstant().plus(23, HOURS).plus(59, MINUTES))),
				Arguments.of(Date.from(naatid.atStartOfDay().atZone(systemDefault()).minus(1, DAYS).toInstant())),
				Arguments.of(Date.from(naatid.atStartOfDay().atZone(ZoneId.of("Z")).toInstant())), // datoMottatt er UTC uten klokkeslett
				Arguments.of(Date.from(naatid.atStartOfDay().atZone(ZoneId.of("Z")).toInstant().minus(1, DAYS))) // datoMottatt er UTC uten klokkeslett
		);
	}

	@ParameterizedTest
	@EnumSource(
			value = InnsynCode.class,
			names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"},
			mode = EXCLUDE)
	void shouldThrowExceptionIfOverstyrInnsynsreglerIsInvalid(InnsynCode overstyrInnsynsregler) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.overstyrInnsynsregler(overstyrInnsynsregler.toString())
				.build();

		Exception e = assertThrows(InputValideringFeiletException.class, () ->
				validator.validateRequest(request, FORSOEKFERDIGSTILL)
		);
		assertThat(e.getMessage()).contains(String.format("Sak.overstyrInnsynsregler må være en av følgende verdier %s. Mottatt: %s", LOVLIGE_INNSYNSKODER, overstyrInnsynsregler));
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"})
	@NullSource
	void shouldNotThrowExceptionWhenOverstyrInnsynsreglerIsValid(InnsynCode overstyrInnsynsregler) {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.overstyrInnsynsregler(overstyrInnsynsregler != null ? overstyrInnsynsregler.toString() : null)
				.build();

		assertDoesNotThrow(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
	}

	@Test
	public void shouldThrowExceptionWhenFysiskDokumentNull() {
		OpprettJournalpostRequest opprettJournalpostRequest = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(null)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build()))
						.build()))
				.build();


		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant(ARKIV).fysiskDokument må være en base64 representert fil større enn 0 bytes");
	}

	@Test
	public void shouldThrowExceptionWhenFysiskDokumentContainsInvalidMagicNumber() {
		OpprettJournalpostRequest opprettJournalpostRequest = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build()))
						.build()))
				.build();


		//FF D8 FF E0 00
		assertThatExceptionOfType(InvalidPdfException.class)
				.isThrownBy(() -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL))
				.withMessage("Dokumenter[0].dokumentvariant(ARKIV).fysiskDokument kan ikke lagres i fagarkivet. fysiskDokument magicNumber={FF D8 FF E0 00} matcher ikke angitt filtype=PDF");
	}

	@Test
	public void shouldThrowExceptionWhenFysiskZeroLength() {
		OpprettJournalpostRequest opprettJournalpostRequest = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument("".getBytes())
										.variantformat(VARIANTFORMAT_ARKIV)
										.build()))
						.build()))
				.build();


		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Dokumenter[0].dokumentvariant(ARKIV).fysiskDokument må være en base64 representert fil større enn 0 bytes");
	}


	@Test
	void shouldThrowExceptionWhenFagsakAndFagsystemPP01AndFagsakIdNotNumeric() {
		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.PP01).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("Sak.fagsakId må være et heltall dersom saken er opprett i PSAK");
	}
}