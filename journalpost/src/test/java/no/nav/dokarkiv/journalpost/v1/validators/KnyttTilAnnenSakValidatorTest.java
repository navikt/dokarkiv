package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest.KnyttTilAnnenSakRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.lang.String.format;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.itest.KnyttTilAnnenSakIT.createKnyttTilAnnenSakRequestHappyPath;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createKnyttTilAnnenSakRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class KnyttTilAnnenSakValidatorTest {

	private static final String SAKSTYPE_FAGSAK = "FAGSAK";
	private static final String SAKSTYPE_GENERELL = "GENERELL_SAK";
	private static final String FAGSAK_ID = "0123A21";
	private static final String FAGSAK_SYSTEM = "IT01";
	private static final String TEMA = "SYK";
	private static final String BRUKER_ID = "12345612345";
	private static final String AKTOER_ID = "12345612345";
	private static final String ORG_NR = "123456789";
	private static final long KILDE_JOURNALPOST_ID = 111111111;
	private static final String JOURNALFOERENDE_ENHET = "9999";
	private static final String FEILMELDING = "Validering feilet for journalpostId=111111111. Feilmelding=%s";

	private final KnyttTilAnnenSakValidator knyttTilAnnenSakValidator = new KnyttTilAnnenSakValidator();

	@Test
	public void shouldValidateRequestWithFnr() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.bruker(Bruker.builder().id(BRUKER_ID).idType(FNR).build())
				.build();

		knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
	}

	@Test
	public void shouldValidateRequestWithAktoerid() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.bruker(Bruker.builder().id(AKTOER_ID).idType(AKTOERID).build())
				.build();

		knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
	}

	@Test
	public void shouldValidateRequestWithOrgnr() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.bruker(Bruker.builder().id(ORG_NR).idType(ORGNR).build())
				.build();

		knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldValidateRequestWithGenerellSakWhenFagsakParamsAreEmptyOrNull(String fagsakParam) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.sakstype(SAKSTYPE_GENERELL)
				.fagsakId(fagsakParam)
				.fagsaksystem(fagsakParam)
				.build();

		knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldThrowInputValideringFeiletExceptionWhenSakstypeIsEmptyOrNull(String sakstype) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.sakstype(sakstype)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("sakstype kan ikke være null eller tom"));
	}

	@Test
	public void shouldThrowInputValideringFeiletExceptionForInvalidSakstype() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.sakstype("INVALID")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("Ugyldig sakstype: INVALID"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldThrowInputValideringFeiletExceptionWhenFagsakIdIsEmptyOrNull(String fagsakId) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.fagsakId(fagsakId)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("fagsakId kan ikke være null eller tom dersom sakstype=FAGSAK"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldThrowInputValideringFeiletExceptionWhenFagsaksystemIsEmptyOrNull(String fagsaksystem) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.fagsaksystem(fagsaksystem)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("fagsaksystem kan ikke være null eller tom dersom sakstype=FAGSAK"));
	}

	@Test
	public void shouldThrowInputValideringFeiletExceptionWhenFagsakIdIsNonemptyForSakstypeGenerellSak() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.sakstype(SAKSTYPE_GENERELL)
				.fagsakId(FAGSAK_ID)
				.fagsaksystem("")
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("fagsakId og fagsaksystem skal ikke oppgis dersom sakstype=GENERELL_SAK"));
	}

	@Test
	public void shouldThrowInputValideringFeiletExceptionWhenFagsaksystemIsNonemptyForSakstypeGenerellSak() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.sakstype(SAKSTYPE_GENERELL)
				.fagsakId("")
				.fagsaksystem(FAGSAK_SYSTEM)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("fagsakId og fagsaksystem skal ikke oppgis dersom sakstype=GENERELL_SAK"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0101011234", "010101123456"})
	public void shouldThrowInputValideringFeiletExceptionWhenLengthOfFnrIsIncorrect(String brukerId) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createKnyttTilAnnenSakRequest(SAKSTYPE_FAGSAK, FAGSAK_ID, FAGSAK_SYSTEM, TEMA, FNR, brukerId, JOURNALFOERENDE_ENHET);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("bruker.id må ha 11 siffer dersom bruker.idType=FNR."));
	}

	@Test
	public void shouldThrowInputValideringFeiletExceptionWhenBrukerIdIsNonNumeric() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.bruker(Bruker.builder().idType(FNR).id("101095134a").build())
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("bruker.id er ikke et tall."));
	}

	@ParameterizedTest
	@ValueSource(strings = {"12345678", "1234567890"})
	public void shouldThrowInputValideringFeiletExceptionWhenLengthOfOrgnrIsIncorrect(String brukerId) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createKnyttTilAnnenSakRequest(SAKSTYPE_FAGSAK, FAGSAK_ID, FAGSAK_SYSTEM, TEMA, ORGNR, brukerId, JOURNALFOERENDE_ENHET);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("bruker.id må ha 9 siffer dersom bruker.idType=ORGNR."));
	}

	@Test
	public void shouldThrowInputValideringFeiletExceptionForIllegalBrukerIdType() {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createKnyttTilAnnenSakRequest(SAKSTYPE_FAGSAK, FAGSAK_ID, FAGSAK_SYSTEM, TEMA, null, "010101123456", JOURNALFOERENDE_ENHET);

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("bruker.idType kan ikke være null eller tom"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldThrowInputValideringFeiletExceptionWhenTemaIsNull(String tema) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.tema(tema)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("tema kan ikke være null eller tom"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"HJ", "HJEM"})
	public void shouldThrowInputValideringFeiletExceptionWhenLengthOfTemaIsIncorrect(String tema) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.tema(tema)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("tema må ha 3 tegn"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldThrowInputValideringFeiletExceptionWhenJournalforendeEnhetIsEmptyOrNull(String journalfoerendeEnhet) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.journalfoerendeEnhet(journalfoerendeEnhet)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("journalfoerendeEnhet kan ikke være null eller tom"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"123", "12345"})
	public void shouldThrowInputValideringFeiletExceptionWhenLengthOfJournalforendeEnhetIsIncorrect(String journalfoerendeEnhet) {
		KnyttTilAnnenSakRequest knyttTilAnnenSakRequest = createStandardKnyttTilAnnenSakRequestBuilder()
				.journalfoerendeEnhet(journalfoerendeEnhet)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, KILDE_JOURNALPOST_ID))
				.withMessageContaining(createFeilmelding("journalfoerendeEnhet må ha 4 siffer"));
	}

	private String createFeilmelding(String melding) {
		return format(FEILMELDING, melding);
	}

	public static KnyttTilAnnenSakRequestBuilder createStandardKnyttTilAnnenSakRequestBuilder() {
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