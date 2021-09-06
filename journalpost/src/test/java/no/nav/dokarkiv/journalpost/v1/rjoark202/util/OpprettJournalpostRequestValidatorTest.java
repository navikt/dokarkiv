package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;

public class OpprettJournalpostRequestValidatorTest {

	public static final String FORSOEKFERDIGSTILL = "false";
	public static final String JOURNALFOERENDE_ENHET = "9999";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private OpprettJournalpostRequest request;

	private OpprettJournalpostRequestValidator validator = new OpprettJournalpostRequestValidator();

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
				.journalfoerendeEnhet(TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("JournalfoerendeEnhet må være null eller fire siffer. JournalfoerendeEnhet=" + TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG);
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}


	@Test
	public void shouldThrowExceptionWhenJournaforendeEnhetIsLotsOfSpaces() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("JournalfoerendeEnhet må være null eller fire siffer. JournalfoerendeEnhet=" + TestUtils.JOURNALFOERENDE_ENHET_UGYLDIG_WHITESPACES);
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tema");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(null).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsakId");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemNotSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(null).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsaksystem");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).arkivsaksnummer(ARKIVSAKSNUMMER).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForFagsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).arkivsaksystem(Arkivsaksystem.GSAK).fagsakId(FAGSAK_ID).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksystem");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenTemaNotSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(null)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tema");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenBrukerNotSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(null)
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsakId(FAGSAK_ID).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsakId");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).fagsaksystem(Fagsaksystem.AO01).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsaksystem");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaknummerSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemSetForGenerellSak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksystem");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenFagsakIdSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).fagsakId(FAGSAK_ID).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsakId");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenFagsaksystemSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).fagsaksystem(Fagsaksystem.AO01).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.fagsaksystem");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksnummerNotSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenJournalfoerendeEnhetEr9999AndForsoekFerdigstillErFalse() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksystem(Arkivsaksystem.GSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Ikke mulig å opprette journalpost på journalfoerendeEnhet=9999");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenArkivsaksystemNotSetForArkivsak() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().sakstype(Sakstype.ARKIVSAK).arkivsaksnummer(ARKIVSAKSNUMMER).build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksystem");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.idType");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeORGNRAndIdMoreThan9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.ORGNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRAndIdNot9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("1010101010")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenAvsenderMottakerIdTypeHPRNRMoreThan9Digits() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn(AVSENDER_NAVN)
						.id("9999999999")
						.idType(AvsenderMottakerIdType.HPRNR)
						.build())
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("AvsenderMottaker.id");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIsMissingId() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.id(null)
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNotNumeric() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("abc11111111")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForFnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForOrgnr() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.ORGNR)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdHasInvalidLengthForAktoerid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.AKTOERID)
						.id("1122334455")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Bruker.id");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfTemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema("tema")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("tema");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfBehandlingstemaIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("behandlingstema")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("behandlingstema");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfInngaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.kanal("kanal")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("kanal");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfMottakskanalTemaCombinationIsInvalid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_FOR)
				.kanal("NAV_NO_UINNLOGGET")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Det er kun mulig å arkivere med mottakskanal NAV_NO_UINNLOGGET dersom tema=SER.");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfUtgaaendeKanalIsInvalid() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.kanal("kanal")
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("kanal");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfSakIsMissingArkivsaksnummer() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer(null)
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfArkivsaksnummerNotNumeric() {
		request = createMinimalRequest(JournalpostType.UTGAAENDE)
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.arkivsaksnummer("quack123")
						.build())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Sak.arkivsaksnummer");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentkategori");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.filtype");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.filtype");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.filtype");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.variantformat");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
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

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.variantformat");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionIfDokumenterIsEmpty() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(new ArrayList<>())
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("dokumenter");

		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldThrowExceptionWhenBehandlingstemaIsNotValid() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.behandlingstema("ab333")
				.avsenderMottaker(null)
				.build();
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Oppgitt behandlingstema=ab333 er ikke på formatet ´ab + fire siffer´.");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

	@Test
	public void shouldNotThrowExceptionIfDifferentVariantformat() {
		request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.dokumenter(List.of(Dokument.builder()
						.dokumentKategori(DOKUMENTKATEGORI_SED)
						.dokumentvarianter(List.of(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
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
										.variantformat(VARIANTFORMAT_ARKIV)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_XML)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build(),
								DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.build()))
						.build()))
				.build();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Dokument.dokumentvariant.variantformat");
		validator.validateRequest(request, FORSOEKFERDIGSTILL);
	}

}