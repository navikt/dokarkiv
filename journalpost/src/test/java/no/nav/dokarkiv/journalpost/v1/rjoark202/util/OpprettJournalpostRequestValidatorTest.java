package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

public class OpprettJournalpostRequestValidatorTest {

	public static final String FORSOEKFERDIGSTILL = "false";
	public static final String JOURNALFOERENDE_ENHET = "9999";

	private OpprettJournalpostRequest request;

	private final OpprettJournalpostRequestValidator validator = new OpprettJournalpostRequestValidator();

	@Test
	public void happyPath() {
		request = createRequest(JournalpostType.INNGAAENDE);

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldNotThrowExceptionIfMottakskanalTemaCombinationIsValid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SER)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldValidateWhenNoAvsenderMottaker() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(null)
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void happyPathGenerellSakTemaUFO() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldJournalfoereWhenJournalfoerendeEnhetEr9999AndForsoekFerdigstillErTrue() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(TestUtils.JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}


	@Test
	public void shouldValidateOkWhenJournaforendeEnhetErNull() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(null)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsNotNullOrNot4Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET_UGYLDIG)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Journalpost.journalfoerendeEnhet må være null eller fire siffer. journalfoerendeEnhet=" + JOURNALFOERENDE_ENHET_UGYLDIG);
	}


	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsLotsOfSpaces() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Journalpost.journalfoerendeEnhet må være null eller fire siffer. journalfoerendeEnhet=" + TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES);
	}

	@Test
	public void happyPathGenerellSakTemaPEN() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenTemaNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(null)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"tema");
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(null).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(null).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).arkivsaksnummer(ARKIVSAKSNUMMER).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).arkivsaksystem(Arkivsaksystem.GSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenTemaNotSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(null)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"tema");
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsaksystem(Fagsaksystem.AO01).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).fagsakId(FAGSAK_ID).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.fagsakId");
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).fagsaksystem(Fagsaksystem.AO01).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.fagsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksnummerNotSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionWhenJournalfoerendeEnhetEr9999AndForsoekFerdigstillErFalse() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Ikke mulig å opprette journalpost på journalfoerendeEnhet=9999");
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemNotSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksystem");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdIsSetButNotIdType() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("11223344556")
						.idType(null)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.idType");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeIsSetAndNotId() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(null)
						.idType(AvsenderMottakerIdType.FNR)
						.navn(AVSENDER_NAVN)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndIdNot11Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("1111111111a")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeFNRAndMoreThan11Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("111111111111")
						.idType(AvsenderMottakerIdType.FNR)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdNot9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("NO7777777")
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"88888888", "1010101010"})
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdLessThan9OrMoreThan9Digits(String orgnr) {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(orgnr)
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"7777777", "88888888", "999999999"})
	public void shouldValidateWhenAvsenderMottakerIdTypeHPRNRAnd7To9Digits(String hprnr) {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("777777a")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@ParameterizedTest
	@ValueSource(strings = {"666666", "1010101010"})
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdLessThan7OrMoreThan9Digits(String hprnr) {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id(hprnr)
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"AvsenderMottaker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingId() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.id(null)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc11111111")
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("1122334455")
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.ORGNR)
						.id("1122334455")
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForAktoerid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.AKTOERID)
						.id("1122334455")
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Bruker.id");
	}

	@Test
	public void shouldThrowExceptionIfTemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema("tema")
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"tema");
	}

	@Test
	public void shouldThrowExceptionIfBehandlingstemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("behandlingstema")
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"behandlingstema");
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.kanal("kanal")
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"kanal");
	}

	@Test
	public void shouldThrowExceptionIfMottakskanalTemaCombinationIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Det er kun mulig å arkivere med mottakskanal NAV_NO_UINNLOGGET dersom tema=SER.");
	}

	@Test
	public void shouldThrowExceptionIfUtgaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.kanal("kanal")
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"kanal");
	}

	@Test
	public void shouldThrowExceptionIfSakIsMissingArkivsaksnummer() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(null)
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionIfArkivsaksnummerNotNumeric() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer("quack123")
						.build())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Sak.arkivsaksnummer");
	}

	@Test
	public void shouldThrowExceptionIfDokumentkategoriIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori("kategori")
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentkategori");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsNotSet() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(null)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.filtype");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype("filtype")
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.filtype");
	}

	@Test
	public void shouldThrowExceptionIfFiltypeIsInvalidForARKIV() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_XML)
								.variantformat(VARIANTFORMAT_ARKIV)
								.build()))
						.build()))
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.filtype");
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsNotSet() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(null)
								.build()))
						.build()))
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.variantformat");
	}

	@Test
	public void shouldThrowExceptionIfVariantformatIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(singletonList(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(singletonList(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat("variantformat")
								.build()))
						.build()))
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.variantformat");
	}

	@Test
	public void shouldThrowExceptionIfDokumenterIsEmpty() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(new ArrayList<>())
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"dokumenter");
	}

	@Test
	public void shouldThrowExceptionWhenBehandlingstemaIsNotValid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("ab333")
				.avsenderMottaker(null)
				.build();

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Oppgitt behandlingstema=ab333 er ikke på formatet ´ab + fire siffer´.");
	}

	@Test
	public void shouldNotThrowExceptionIfDifferentVariantformat() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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
	public void shouldThrowExceptionIfDuplicateVariantformat() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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

		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(request, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.variantformat");
	}

	@ParameterizedTest
	@EnumSource(
			value = InnsynCode.class,
			names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"},
			mode = EXCLUDE)
	void shouldThrowExceptionIfOverstyrInnsynsreglerIsInvalid(InnsynCode overstyrInnsynsregler) {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(Sakstype.GENERELL_SAK)
						.build())
				.overstyrInnsynsregler(overstyrInnsynsregler.toString())
				.build();

		Exception e = assertThrows(InputValideringFeiletException.class, () ->
				validator.validateRequest(request, FORSOEKFERDIGSTILL)
		);
		assertTrue(e.getMessage().contains("Sak.overstyrInnsynsregler kan kun ta verdiene"));
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"})
	@NullSource
	void shouldNotThrowExceptionWhenOverstyrInnsynsreglerIsValid(InnsynCode overstyrInnsynsregler) {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
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


		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.fysiskDokument må være satt med en base64 representert fil større en 0 bytes.");
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


		assertThrows(InputValideringFeiletException.class,
				() -> validator.validateRequest(opprettJournalpostRequest, FORSOEKFERDIGSTILL),
				"Dokument.dokumentvariant.fysiskDokument må være satt med en base64 representert fil større en 0 bytes.");
	}

	@Test
	void shouldThrowExceptionWhenFagsakAndFagsystemPP01AndFagsakIdNotNumeric() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.PP01).build())
				.build();

		assertThrows(InputValideringFeiletException.class, () -> {
			validator.validateRequest(request, FORSOEKFERDIGSTILL);
		}, "Sak.fagsakId skal være opprettet i PSAK og må være et numerisk heltall.");
	}
}