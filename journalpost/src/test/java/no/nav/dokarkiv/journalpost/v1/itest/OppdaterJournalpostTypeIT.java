package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype.OppdaterJournalposttypeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JANUARY_1_2020;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_JOURNALPOSTTYPE;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class OppdaterJournalpostTypeIT extends AbstractJournalpostIT {

	private static final String EXPECTED_ENHET = "1234";
	private static final String UTGAAENDE = "UTGAAENDE";
	private static final String OPPDATERJOURNALPOSTTYPE_URL = "/oppdaterJournalposttype";
	private static final String SRV_JOARKADMIN = "srvjoarkadmin";
	private static final String NOTAT = "NOTAT";

	@AfterEach
	public void cleanUp() {
		super.cleanup();
	}

	@Test
	public void happyPathNotat() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequest(NOTAT, EXPECTED_ENHET);
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();
		List<AksjonsLogg> aksjonsLogg = aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpostId);
		validateUpdatedJournalpostNotat(changedJournalpost);
		validateAksjonslogg(aksjonsLogg.get(0), journalpostId, N);
	}

	@Test
	public void happyPathUtgaaende() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, J));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequest(UTGAAENDE, EXPECTED_ENHET);
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();
		validateUpdatedJournalpostUtgaaende(changedJournalpost);
		List<AksjonsLogg> aksjonsLogg = aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpostId);
		validateAksjonslogg(aksjonsLogg.get(0), journalpostId, JournalpostTypeCode.U);
	}

	@Test
	public void shouldNotChangeJournalforendeEnhetWhenJournalforendeEnhetIsNotGivenInRequest() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, U).journalForendeEnhetId("5678"));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequestWithoutJournalforendeEnhet();
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();
		validateBaseJournalpost(changedJournalpost, "5678");
	}

	@Test
	public void shouldReturnBadRequestWhenJournalpostIsNotIngaaende() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(JournalpostTypeCode.U, U).journalForendeEnhetId("5678"));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequestWithoutJournalforendeEnhet();
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();

		String expectedErrorMessage = format("Kunne ikke oppdatere journalposttype for journalpost med journalpostId=%s. Journalpost med journalpostId=%s har journalposttype=U og kan derfor ikke endres. Kun journalposter med journalposttype=I kan endres.", journalpostId, journalpostId);
		validateBadRequestResponse(responseEntity, expectedErrorMessage, changedJournalpost, U);
	}

	@Test
	public void shouldReturnBadRequestWhenJournalpostIsInWrongStatus() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, A).journalForendeEnhetId("5678"));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequestWithoutJournalforendeEnhet();
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();

		String expectedErrorMessage = format("Kunne ikke oppdatere journalposttype for journalpost med journalpostId=%s. Journalpost med journalpostId=%s har journalstatus=A og kan derfor ikke endres. Kun journalposter med journalstatus=[J, M, U, MO, UB] kan endres.", journalpostId, journalpostId);
		validateBadRequestResponse(responseEntity, expectedErrorMessage, changedJournalpost, A);
	}

	@Test
	public void shouldReturnBadRequestWhenJournalforendeEnhetIsInvalid() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M).journalForendeEnhetId("5678"));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequest(UTGAAENDE, "12345");
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();

		String expectedErrorMessage = format("Kunne ikke oppdatere journalposttype for journalpost med journalpostId=%s. Ugyldig journalfoerendeEnhet, må være 4 siffer. Mottok: 12345", journalpostId);
		validateBadRequestResponse(responseEntity, expectedErrorMessage, changedJournalpost, M);
	}

	@Test
	public void shouldReturnBadRequestWhenTypeEndresTilIsInvalid() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M).journalForendeEnhetId("5678"));

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequest("INVALID", "1234");
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()) + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		commitAndStartNewTransaction();
		Journalpost changedJournalpost = journalpostTestRepository.findById(journalpostId).get();

		String expectedErrorMessage = format("Kunne ikke oppdatere journalposttype for journalpost med journalpostId=%s. Ugyldig typeEndresTil, kan kun endres til UTGAAENDE eller NOTAT. Mottok:", journalpostId);
		validateBadRequestResponse(responseEntity, expectedErrorMessage, changedJournalpost, M);
	}

	@Test
	public void shouldReturn404WHenNoJournalpost() {

		OppdaterJournalposttypeRequest request = createPatchOppdaterJournalpostTypeRequest(UTGAAENDE, "1234");
		HttpEntity<OppdaterJournalposttypeRequest> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(apiJournalpostPath("123456789") + OPPDATERJOURNALPOSTTYPE_URL, PATCH, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(responseEntity.getBody()).contains("Fant ingen journalpost med journalpostId=123456789 i arkivet");
	}

	public static JournalpostBuilder buildJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		return JournalpostTestDataProvider.buildJournalpost(journalpostType, journalStatus)
				.avsenderMottakerIdType(AvsenderMottakerIdTypeCode.FNR);
	}

	private void validateBadRequestResponse(ResponseEntity<String> responseEntity, String expectedMessage, Journalpost changedJournalpost, JournalStatusCode journalStatusCode) {
		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody()).contains(expectedMessage);
		assertThat(changedJournalpost.getEndretAvNavn()).isNullOrEmpty();
		assertThat(changedJournalpost.getEndretKildeNavn()).isNullOrEmpty();
		assertThat(changedJournalpost.getJournalstatus()).isEqualTo(journalStatusCode);
	}

	private void validateAksjonslogg(AksjonsLogg aksjonsLogg, Long journalpostId, JournalpostTypeCode nyJournalpostTypeCode) {
		assertThat(aksjonsLogg.getAksjon()).isEqualTo(ENDRE_JOURNALPOSTTYPE);
		assertThat(aksjonsLogg.getJournalpostId()).isEqualTo(journalpostId);
		ArkivElementEndring endring = aksjonsLogg.getArkivElementEndringer().stream().findFirst().get();

		assertThat(endring.getArkivElement()).isEqualTo("Journalpost.journalpost_type");
		assertThat(endring.getFraVerdi()).isEqualTo("I");
		assertThat(endring.getTilVerdi()).isEqualTo(nyJournalpostTypeCode.name());
	}

	private void validateUpdatedJournalpostUtgaaende(Journalpost updatedJournalpost) {
		assertThat(updatedJournalpost.getJournalposttype()).isEqualTo(JournalpostTypeCode.U);
		assertThat(updatedJournalpost.getUtsendingskanal()).isEqualTo(L);
		assertThat(updatedJournalpost.getJournalstatus()).isEqualTo(JournalStatusCode.FL);

		assertThat(updatedJournalpost.getAvsenderMottakerId()).isEqualTo("1");
		assertThat(updatedJournalpost.getAvsenderMottaker()).isEqualTo("Bjarne Betjent");
		assertThat(updatedJournalpost.getAvsenderMottakerIdType()).isEqualTo(AvsenderMottakerIdTypeCode.FNR);

		validateBaseJournalpost(updatedJournalpost);
	}

	private void validateUpdatedJournalpostNotat(Journalpost updatedJournalpost) {
		assertThat(updatedJournalpost.getJournalposttype()).isEqualTo(N);
		assertThat(updatedJournalpost.getJournalstatus()).isEqualTo(JournalStatusCode.D);
		assertThat(updatedJournalpost.getAvsenderMottakerId()).isNull();
		assertThat(updatedJournalpost.getAvsenderMottaker()).isNull();
		assertThat(updatedJournalpost.getAvsenderMottakerIdType()).isNull();

		validateBaseJournalpost(updatedJournalpost);
	}

	private void validateBaseJournalpost(Journalpost updatedJournalpost) {
		validateBaseJournalpost(updatedJournalpost, EXPECTED_ENHET);
	}

	private void validateBaseJournalpost(Journalpost updatedJournalpost, String expectedEnhet) {
		assertThat(updatedJournalpost.getJournalForendeEnhetId()).isEqualTo(expectedEnhet);
		assertThat(updatedJournalpost.getMottakskanal()).isEqualTo(NAV_NO);
		assertThat(updatedJournalpost.getMottattDato()).isEqualTo(JANUARY_1_2020);
		assertThat(updatedJournalpost.getEndretAvNavn()).isEqualTo(SRV_JOARKADMIN);
		assertThat(updatedJournalpost.getEndretKildeNavn()).isEqualTo(SRV_JOARKADMIN);

		ChangeStamp changestamp = updatedJournalpost.getChangeStamp();

		assertThat(changestamp.getUpdatedDate()).isAfter(now().minusSeconds(10));
		assertThat(changestamp.getUpdatedBy()).isEqualTo(SRV_JOARKADMIN);
	}

	private OppdaterJournalposttypeRequest createPatchOppdaterJournalpostTypeRequest(String typeEndresTil, String journalfoerendeEnhet) {
		return new OppdaterJournalposttypeRequest(typeEndresTil, journalfoerendeEnhet);
	}


	private OppdaterJournalposttypeRequest createPatchOppdaterJournalpostTypeRequestWithoutJournalforendeEnhet() {
		return new OppdaterJournalposttypeRequest(OppdaterJournalpostTypeIT.UTGAAENDE, null);
	}
}
