package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem.GSAK;
import static no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType.HPRNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequestWithoutEksternReferanseId;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.SKJULT_TITTEL;
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
		OpprettJournalpostRequest request = createRequest(INNGAAENDE);

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldNotThrowExceptionIfMottakskanalTemaCombinationIsValid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_SER)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldValidateWhenNoAvsenderMottaker() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(null)
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathGenerellSakTemaUFO() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	void shouldThrowExceptionWhenJournalposttypeIsNull() {
		JournalpostType journalpostType = null;
		OpprettJournalpostRequest request = createMinimalRequest(journalpostType)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("journalposttype må være satt.");
	}

	@ParameterizedTest
	@EnumSource(value = JournalpostType.class, names = {"INNGAAENDE"}, mode = EXCLUDE)
	public void shouldJournalfoereWhenJournalfoerendeEnhetEr9999AndJournpostTypeErUlikInngaaendeAndForsoekFerdigstillErTrue(JournalpostType journalpostType) {
		OpprettJournalpostRequest request = createMinimalRequest(journalpostType)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldValidateOkWhenJournaforendeEnhetErNull() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.journalfoerendeEnhet(null)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsNotNullOrNot4Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET_UGYLDIG)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("journalfoerendeEnhet må være null eller fire siffer. Mottatt journalfoerendeEnhet=" + JOURNALFOERENDE_ENHET_UGYLDIG);
	}

	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsLotsOfSpaces() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("journalfoerendeEnhet må være null eller fire siffer. Mottatt journalfoerendeEnhet=" + JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES);
	}

	@Test
	public void happyPathGenerellSakTemaPEN() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenTemaNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(null)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("tema");
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("bruker");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdNotSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(null).fagsaksystem(AO01).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemNotSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(null).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).arkivsaksnummer(ARKIVSAKSNUMMER).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForFagsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).arkivsaksystem(GSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(GENERELL_SAK).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("bruker");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).fagsaksystem(AO01).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForGenerellSak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(GENERELL_SAK).arkivsaksystem(GSAK).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.sak(Sak.builder().sakstype(ARKIVSAK).fagsakId(FAGSAK_ID).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.sak(Sak.builder().sakstype(ARKIVSAK).fagsaksystem(AO01).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		var exception = assertThrows(InputValideringFeiletException.class, () -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
		assertThat(exception.getMessage()).contains("sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksnummerNotSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenJournalfoerendeEnhetEr9999AndJournalpostTypeErInngaaendeAndForsoekFerdigstillErFalse() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.journalfoerendeEnhet(MASKINELL_JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksystem(GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Ikke mulig å opprette journalpost med type inngaaende på journalfoerendeEnhet=9999 (maskinell) så lenge journalposten ikke forsøkes å ferdigstilles");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemNotSetForArkivsak() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.sak(Sak.builder().sakstype(ARKIVSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdIsSetButNotIdType() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("11223344556")
						.idType(null)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.idType");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeIsSetAndNotId() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(null)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndIdNot11Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("1111111111a")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndMoreThan11Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("111111111111")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdNot9Digits() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("NO7777777")
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"88888888", "1010101010"})
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdLessThan9OrMoreThan9Digits(String orgnr) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(orgnr)
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"7777777", "88888888", "999999999"})
	public void shouldValidateWhenAvsenderMottakerIdTypeHPRNRAnd7To9Digits(String hprnr) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(hprnr)
						.idType(HPRNR)
						.build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdNotANumber() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("777777a")
						.idType(HPRNR)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"666666", "1010101010"})
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdLessThan7OrMoreThan9Digits(String hprnr) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(hprnr)
						.idType(HPRNR)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("avsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldThrowExceptionIfBrukerIsMissingId(String brukerId) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder()
						.id(brukerId)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("bruker.id må være satt.");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingIdType() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder()
						.id(BRUKER_ID_PERSON)
						.idType(null)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("bruker.idType må være satt.");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc11111111")
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("bruker.id må bestå av tall.");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("1122334455")
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.ORGNR)
						.id("1122334455")
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForAktoerid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(AKTOERID)
						.id("1122334455")
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfTemaIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema("tema")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("tema");
	}

	@Test
	public void shouldThrowExceptionIfBehandlingstemaIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.behandlingstema("behandlingstema")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("behandlingstema");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeKanalIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.kanal("kanal")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("kanal");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeAndMottaksKanalIsNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.kanal(null)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("kanal er påkrevd for inngående journalposter");
	}

	@Test
	public void shouldThrowExceptionIfMottakskanalTemaCombinationIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Det er kun mulig å arkivere med mottakskanal=NAV_NO_UINNLOGGET dersom tema=SER");
	}

	@Test
	public void shouldThrowExceptionIfUtgaaendeKanalIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(UTGAAENDE)
				.kanal("kanal")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("kanal");
	}

	@Test
	public void shouldThrowExceptionIfSakIsMissingArkivsaksnummer() {
		OpprettJournalpostRequest request = createMinimalRequest(UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(GSAK)
						.arkivsaksnummer(null)
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class).
				isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionIfArkivsaksnummerNotNumeric() {
		OpprettJournalpostRequest request = createMinimalRequest(UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(GSAK)
						.arkivsaksnummer("quack123")
						.build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionIfDokumentkategoriIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori("kategori")
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentKategori validerer ikke mot kodeverk. Gyldige verdier for dokumentKategori er %s. Mottatt dokumentKategori=kategori"
						.formatted(Arrays.toString(DokumentKategoriCode.values())));
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(null)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].filtype må være satt for variantformat=ARKIV");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype("filtype")
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].filtype validerer ikke mot kodeverk for variantformat=ARKIV. Gyldige verdier for filtype er");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalidForARKIV() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_XML)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].filtype må være PDF, PDFA eller XLSX for variantformat=ARKIV");
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsNotSet() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(null)
								.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].variantformat må være satt");
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsInvalid() {
		String variantformat = "UGYLDIG_VARIANTFORMAT";
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(variantformat)
								.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].variantformat validerer ikke mot kodeverk. Gyldige verdier for variantformat er %s. Mottatt variantformat=%s".formatted(
						Arrays.toString(VariantFormatCode.values()), variantformat));
	}

	@Test
	public void shouldThrowExceptionIfDokumenterIsNull() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(null)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("Kan ikke opprette journalpost uten dokumenter.");
	}

	@Test
	public void shouldThrowExceptionIfDokumenterIsEmpty() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(new ArrayList<>())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("Kan ikke opprette journalpost uten dokumenter.");
	}

	@ParameterizedTest
	@MethodSource
	public void shouldThrowExceptionIfTilleggsopplysningerIsInvalid(String nokkel, String verdi, String feltnavn) {
		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE)
				.tilleggsopplysninger(List.of(new Tilleggsopplysning(nokkel, verdi)))
				.dokumenter(singletonList(Dokument.builder()
						.tittel(DOKUMENT_TITTEL1)
						.brevkode(BREVKODE1)
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.fysiskDokument(FYSISK_DOKUMENT)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build()))
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("tilleggsopplysninger[] kan ikke inneholde tilleggsopplysning der %s er null eller blank".formatted(feltnavn));
	}

	private static Stream<Arguments> shouldThrowExceptionIfTilleggsopplysningerIsInvalid() {
		return Stream.of(
				Arguments.of(null, "verdi", "nokkel"),
				Arguments.of("", "verdi", "nokkel"),
				Arguments.of(" ", "verdi", "nokkel"),
				Arguments.of("nokkel", null, "verdi"),
				Arguments.of("nokkel", "", "verdi"),
				Arguments.of("nokkel", " ", "verdi"),
				Arguments.of(null, null, "nokkel") // nokkel blir sjekket først
		);
	}

	@Test
	public void shouldThrowExceptionWhenBehandlingstemaIsNotValid() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.behandlingstema("ab333")
				.avsenderMottaker(null)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessage("behandlingstema må være på formatet ´ab + 4 siffer´, f.eks. ´ab0256´. Mottatt behandlingstema=ab333");
	}

	@Test
	public void shouldNotThrowExceptionIfDifferentVariantformat() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
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
	public void shouldThrowExceptionWhenVariantformatArkivIsMissing() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
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
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.build()))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Alle dokumenter må innholde en dokumentvariant av typen ARKIV");
	}

	@Test
	public void shouldThrowExceptionWhenDocumentHasDuplicateVariantformat() {
		String tittel = "dokumentMedDuplikateDokumentvarianter";
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.tittel(tittel)
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
				.withMessage("dokumenter[0].dokumentvarianter[].variantformat må være unik. Fant følgende duplikater for dokument med tittel=%s: variantformat=%s funnet 2 ganger"
						.formatted(tittel, VARIANTFORMAT_ORIGINAL));
	}

	@Test
	public void shouldThrowExceptionWhenADocumentHasMultipleVariantformatArkiv() {
		String tittel = "dokumentMedDuplikateDokumentvarianter";
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.tittel(tittel)
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
				.withMessage("dokumenter[0].dokumentvarianter[].variantformat må være unik. Fant følgende duplikater for dokument med tittel=%s: variantformat=%s funnet 2 ganger"
						.formatted(tittel, VARIANTFORMAT_ARKIV));
	}

	@Test
	public void shoudThrowExeceptionIfNotAtleastOneDocumentIsPresent() {
		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
				.eksternReferanseId("eksternReferanseId")
				.journalposttype(INNGAAENDE)
				.tema(FagomradeCode.FOR.name())
				.kanal("NAV_NO")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("Kan ikke opprette journalpost uten dokumenter");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"alfanumeriskString1",
			"65efa501-1554-4538-a553-1db5b31ad40b",
			"StrengMed\\backslash",
			"epost@adresse.noe",
			"AlleGyldigeTegn2:;,.=-_~$&+*\"\\@!"}
	)
	void validEksternReferanseId(String eksternReferanseId) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.eksternReferanseId(eksternReferanseId)
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@ParameterizedTest
	@MethodSource("feilEksternReferanseId")
	void shouldThrowExceptionWhenEksternReferanseIdIsMalformedOrEmpty(String eksternReferanseId, String forventetFeilmelding) {
		OpprettJournalpostRequest request = createMinimalRequestWithoutEksternReferanseId(INNGAAENDE)
				.eksternReferanseId(eksternReferanseId)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining(forventetFeilmelding);

	}

	private static Stream<Arguments> feilEksternReferanseId() {
		return Stream.of(
				Arguments.of("bj5bzAng3tvvY7ao0A15Kj8lq3RuN78rPTDYQp9lz416At7egwxVKw3klqZngX39eYdwqDIs6KUbGurS97R78Mz25WO3r7ththg8QVf2HY1col7713VLSSFHvQKHzftl2aKIXF48pnftmwbNX201aX2msQDb8G8nd31gyzfvzZvYX0hcPeU9g5nm5NeV43RLRaKyR1BLG",
						"eksternReferanseId kan ikke være over 200 tegn. Mottatt eksternReferanseId=bj5bzAng3tvvY7ao0A15Kj8lq3RuN78rPTDYQp9lz416At7egwxVKw3klqZngX39eYdwqDIs6KUbGurS97R78Mz25WO3r7ththg8QVf2HY1col7713VLSSFHvQKHzftl2aKIXF48pnftmwbNX201aX2msQDb8G8nd31gyzfvzZvYX0hcPeU9g5nm5NeV43RLRaKyR1BLG"),
				Arguments.of("ØÆÅhører og mellomrom hører ikke hjemme i url og dermed i eksternReferanseId",
						"eksternReferanseId kan bare inneholde alfanumeriske tegn og følgende spesialtegn :;,.=-_~$&+*\"\\@! Mottatt eksternReferanseId=ØÆÅhører og mellomrom hører ikke hjemme i url og dermed i eksternReferanseId"),
				Arguments.of("",
						"eksternReferanseId kan ikke være null eller tomt"),
				Arguments.of(" ",
						"eksternReferanseId kan ikke være null eller tomt"),
				Arguments.of(null,
						"eksternReferanseId kan ikke være null eller tomt")
		);
	}

	@Test
	void shouldThrowExceptionWhenDatoIsInTheFuture() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.behandlingstema("ab0001")
				.avsenderMottaker(null)
				.datoDokument(LocalDateTime.now().plusDays(3))
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining(format("Validering av %s feilet. Dato kan ikke være frem i tid.", "datoDokument"));
	}

	@ParameterizedTest
	@MethodSource
	void shouldLogWarningWhenDatoMottattIsAfter(LocalDateTime innsendtDato) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.behandlingstema("ab0001")
				.avsenderMottaker(null)
				.datoMottatt(innsendtDato)
				.build();

		assertDoesNotThrow(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
	}

	private static Stream<Arguments> shouldLogWarningWhenDatoMottattIsAfter() {
		var naatid = LocalDateTime.now();

		return Stream.of(
				Arguments.of(naatid.plusHours(1))
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldNotLogWarningWhenDatoMottattIsBeforeOrSameDate(LocalDateTime innsendtDato) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.behandlingstema("ab0001")
				.avsenderMottaker(null)
				.datoMottatt(innsendtDato)
				.build();

		assertDoesNotThrow(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
	}

	private static Stream<Arguments> shouldNotLogWarningWhenDatoMottattIsBeforeOrSameDate() {
		var naatid = LocalDateTime.now();

		return Stream.of(
				Arguments.of(naatid),
				Arguments.of(naatid.minusDays(1))
		);
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, mode = EXCLUDE, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"})
	void shouldThrowExceptionIfOverstyrInnsynsreglerIsInvalid(InnsynCode overstyrInnsynsregler) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.overstyrInnsynsregler(overstyrInnsynsregler.name())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining(format("overstyrInnsynsregler må være en av følgende verdier: null eller %s. Mottatt: %s", LOVLIGE_INNSYNSKODER, overstyrInnsynsregler));
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"})
	@NullSource
	void shouldNotThrowExceptionWhenOverstyrInnsynsreglerIsValid(InnsynCode overstyrInnsynsregler) {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.overstyrInnsynsregler(overstyrInnsynsregler != null ? overstyrInnsynsregler.name() : null)
				.build();

		assertDoesNotThrow(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL));
	}

	@Test
	public void shouldThrowExceptionWhenFysiskDokumentNull() {
		OpprettJournalpostRequest opprettJournalpostRequest = createMinimalRequest(INNGAAENDE)
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

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].fysiskDokument for variantformat=ARKIV må være en base64 representert fil større enn 0 bytes");
	}

	@Test
	public void shouldThrowExceptionWhenFysiskDokumentContainsInvalidMagicNumber() {
		OpprettJournalpostRequest opprettJournalpostRequest = createMinimalRequest(INNGAAENDE)
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
				.withMessage("dokumenter[0].dokumentvarianter[].fysiskDokument med variantformat=ARKIV kan ikke lagres i fagarkivet. fysiskDokument magicNumber={FF D8 FF E0 00} matcher ikke angitt filtype=PDF");
	}

	@Test
	public void shouldThrowExceptionWhenFysiskZeroLength() {
		OpprettJournalpostRequest opprettJournalpostRequest = createMinimalRequest(INNGAAENDE)
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

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL))
				.withMessageContaining("dokumenter[0].dokumentvarianter[].fysiskDokument for variantformat=ARKIV må være en base64 representert fil større enn 0 bytes");
	}


	@Test
	void shouldThrowExceptionWhenFagsakAndFagsystemPP01AndFagsakIdNotNumeric() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(PP01).build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("sak.fagsakId må være et heltall dersom saken er opprett i PSAK");
	}

	@Test
	void shouldThrowExceptionWhenJournalpostTittelIsSkjult() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tittel(SKJULT_TITTEL)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> validator.validateRequest(request, FORSOEKFERDIGSTILL))
				.withMessageContaining("tittel kan ikke være " + SKJULT_TITTEL);
	}

}