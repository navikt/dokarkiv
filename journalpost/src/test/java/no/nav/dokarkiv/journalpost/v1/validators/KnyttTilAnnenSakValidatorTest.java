package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.itest.KnyttTilAnnenSakIT.createKnyttTilAnnenSakRequestHappyPath;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createKnyttTilAnnenSakRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KnyttTilAnnenSakValidatorTest {

    private static final String SAKSTYPE_FAGSAK = "FAGSAK";
    private static final String SAKSTYPE_GENERELL = "GENERELL_SAK";
    private static final String FAGSAK_ID = "0123A21";
    private static final String FAGSAK_SYSTEM = "IT01";
    private static final String TEMA = "SYK";
    private static final String BRUKER_ID = "12345612345";
    private static final String AKTOER_ID = "12345612345";
    private static final String ORG_NR = "123456789";
    private static final String KILDE_JOURNALPOST_ID = "111111111";
    private static final String JOURNALFOERENDE_ENHET = "9999";
    private static final String FEILMELDING = "Validering feilet for journalpostId=111111111. Feilmelding=%s";

    private KnyttTilAnnenSakValidator knyttTilAnnenSakValidator = new KnyttTilAnnenSakValidator();

    // Positive tester
    @Test
    public void shouldValidateRequestWithFnr() {
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().bruker(Bruker.builder().id(BRUKER_ID).idType(FNR).build()).build();
        knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
    }

    @Test
    public void shouldValidateRequestWithAktoerid() {
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().bruker(Bruker.builder().id(AKTOER_ID).idType(AKTOERID).build()).build();
        knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
    }

    @Test
    public void shouldValidateRequestWithOrgnr() {
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().bruker(Bruker.builder().id(ORG_NR).idType(ORGNR).build()).build();
        knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
    }

    @Test
    public void shouldValidateRequestWithGenerellSakAndEmptyFagsakParams() {
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype(SAKSTYPE_GENERELL).fagsakId("").fagsaksystem("").build();
        knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
    }

    @Test
    public void shouldValidateRequestWithGenerellSakAndNullFagsakParams() {
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype(SAKSTYPE_GENERELL).fagsakId(null).fagsaksystem(null).build();
        knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
    }

    // Negative tester på journalpostId
    @Test
    public void shouldThrowInputValideringFeiletExceptionForNullJournalpostId() {
        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(createKnyttTilAnnenSakRequestHappyPath(), null));
        assertEquals("Validering feilet for journalpostId=null. Feilmelding=kildeJournalpostId er ikke et tall.",
                thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNonNumericJournalpostId() {
        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(createKnyttTilAnnenSakRequestHappyPath(), "123456ab78"));
        assertEquals("Validering feilet for journalpostId=123456ab78. Feilmelding=kildeJournalpostId er ikke et tall.",
                thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForEmptyJournalpostId() {

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(createKnyttTilAnnenSakRequestHappyPath(), ""));
        assertEquals("Validering feilet for journalpostId=. Feilmelding=kildeJournalpostId er ikke et tall.",
                thrownException.getMessage());
    }

    // Negative tester på payload
    @Test
    public void shouldThrowInputValideringFeiletExceptionForEmptySakstype() {
        String expectedMessage = createFeilmelding("Sakstype kan ikke være null eller tom");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype("").build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNullSakstype() {
        String expectedMessage = createFeilmelding("Sakstype kan ikke være null eller tom");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype(null).build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForInvalidSakstype() {
        String expectedMessage = createFeilmelding("Ugyldig sakstype: INVALID");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype("INVALID").build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForMissingRequiredFagsakId() {
        String expectedMessage = createFeilmelding("FagsakId kan ikke være null eller tom for sakstype FAGSAK");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().fagsakId("").build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForMissingRequiredFagsaksystem() {
        String expectedMessage = createFeilmelding("Fagsaksystem kan ikke være null eller tom sakstype FAGSAK");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().fagsaksystem("").build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNonemptyFagsakId() {
        String expectedMessage = createFeilmelding("FagsakId og fagsaksystem skal ikke oppgis for sakstype GENERELL_SAK");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype(SAKSTYPE_GENERELL).fagsakId(FAGSAK_ID).fagsaksystem("").build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNonemptyFagsaksystem() {
        String expectedMessage = createFeilmelding("FagsakId og fagsaksystem skal ikke oppgis for sakstype GENERELL_SAK");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().sakstype(SAKSTYPE_GENERELL).fagsakId("").fagsaksystem(FAGSAK_SYSTEM).build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooLongFnr() {
        shouldThrowInputValideringFeiletExceptionForInvalidIdLength(FNR, "010101123456", "Fnr må ha 11 siffer.");
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooShortFnr() {
        shouldThrowInputValideringFeiletExceptionForInvalidIdLength(FNR, "0101011234", "Fnr må ha 11 siffer.");
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNonNumericBrukerId() {
        String expectedMessage = createFeilmelding("Id er ikke et tall.");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().bruker(Bruker.builder().idType(FNR).id("101095134a").build()).build();

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooLongOrgnr() {
        shouldThrowInputValideringFeiletExceptionForInvalidIdLength(ORGNR, "1234567890", "Orgnr må ha 9 siffer.");
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooShortOrgnr() {
        shouldThrowInputValideringFeiletExceptionForInvalidIdLength(ORGNR, "12345678", "Orgnr må ha 9 siffer.");
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForIllegalUseridType() {
        shouldThrowInputValideringFeiletExceptionForInvalidIdLength(null, "010101123456", "idType kan ikke være null eller tom");
    }

    private void shouldThrowInputValideringFeiletExceptionForInvalidIdLength(BrukerIdType brukeridtype, String brukerid, String excpectedMessage) {
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createKnyttTilAnnenSakRequest(SAKSTYPE_FAGSAK, FAGSAK_ID, FAGSAK_SYSTEM, TEMA, brukeridtype, brukerid, JOURNALFOERENDE_ENHET);

        Exception thrownException = Assertions.assertThrows(InputValideringFeiletException.class, () -> knyttTilAnnenSakValidator
                .validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(createFeilmelding(excpectedMessage), thrownException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNullTema() {
        String expectedMessage = createFeilmelding("Tema kan ikke være null eller tom");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().tema(null).build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTomTema() {
        String expectedMessage = createFeilmelding("Tema kan ikke være null eller tom");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().tema("").build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooLongTema() {
        String expectedMessage = createFeilmelding("Tema må ha 3 tegn");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().tema("HJEM").build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooShortTema() {
        String expectedMessage = createFeilmelding("Tema må ha 3 tegn");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().tema("HJ").build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForNullJournalforendeEnhet() {
        String expectedMessage = createFeilmelding("JournalfoerendeEnhet kan ikke være null eller tom");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().journalfoerendeEnhet(null).build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTomJournalforendeEnhet() {
        String expectedMessage = createFeilmelding("JournalfoerendeEnhet kan ikke være null eller tom");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().journalfoerendeEnhet("").build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooLongJournalforendeEnhet() {
        String expectedMessage = createFeilmelding("JournalfoerendeEnhet må ha 4 siffer");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().journalfoerendeEnhet("12345").build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldThrowInputValideringFeiletExceptionForTooShortJournalforendeEnhet() {
        String expectedMessage = createFeilmelding("JournalfoerendeEnhet må ha 4 siffer");
        KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder().journalfoerendeEnhet("123").build();

        Exception actualException = Assertions.assertThrows(InputValideringFeiletException.class, () ->
                knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID));

        assertEquals(expectedMessage, actualException.getMessage());
    }

    private String createFeilmelding(String melding) {
        return String.format(FEILMELDING, melding);
    }

    public static KnyttTilAnnenSakRequest.KnyttTilAnnenSakRequestBuilder createStandardKnyttTilAnnenSakRequestBuilder() {
        KnyttTilAnnenSakRequest requestHappyPath = createKnyttTilAnnenSakRequestHappyPath();
        return KnyttTilAnnenSakRequest.builder()
                .sakstype(requestHappyPath.getSakstype())
                .fagsakId(requestHappyPath.getFagsakId())
                .fagsaksystem(requestHappyPath.getFagsaksystem())
                .tema(requestHappyPath.getTema())
                .bruker(requestHappyPath.getBruker())
                .journalfoerendeEnhet(requestHappyPath.getJournalfoerendeEnhet());
    }
}