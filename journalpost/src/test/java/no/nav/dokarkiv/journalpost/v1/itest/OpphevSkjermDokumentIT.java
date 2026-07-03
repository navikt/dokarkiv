package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.assertj.core.groups.Tuple;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentHjemmelCode;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class OpphevSkjermDokumentIT extends AbstractJournalpostIT {

	private static final String SKJERM_DOKUMENT = "skjermDokument";
	private static final String OPPHEV_SKJERM_DOKUMENT = "opphevSkjermDokument";

	@Test
	void skalFjerneSkjermingFraJournalpostNaarJournalpostenErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		skjermDokument(dokumentInfoId, SkjermDokumentHjemmelCode.ARK);

		commitAndStartNewTransaction();

		Journalpost skjermetJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(skjermetJournalpost.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);

		opphevSkjermDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId, null));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()),
			tuple(RELASJON_SKJERMING_TYPE, SkjermingTypeCode.ARK.name(), null));
	}

	@Test
	void skalGiBadRequestNarDokumentIkkeErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), OPPHEV_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	void skalReturnereNotFoundNaarDokumentIkkeFinnes() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1234", OPPHEV_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	void skalReturnereUnauthorizedNaarTokenIkkeErOboToken() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithClientCredentialToken());

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", OPPHEV_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	void skalReturnereForbiddenNaarBrukerManglerJoarkVedlikeholdGruppe() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITHOUT_GROUP_ACCESS));

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", OPPHEV_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
	}

	private void skjermDokument(Long dokumentInfoId, SkjermDokumentHjemmelCode hjemmel) {
		var request = new SkjermDokumentRequest(hjemmel);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	private ResponseEntity<String> opphevSkjermDokument(Long dokumentInfoId) {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), OPPHEV_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
		return response;
	}

	private void assertAksjonsloggEntries(List<AksjonsLogg> aksjonsLoggList, Tuple... expectedEntries) {
		assertThat(aksjonsLoggList)
			.extracting(AksjonsLogg::getAksjon, AksjonsLogg::getJournalpostId, AksjonsLogg::getDokumentInfoId, AksjonsLogg::getHjemmel)
			.containsExactlyInAnyOrder(expectedEntries);
	}

	private void assertArkivElementEndringer(List<AksjonsLogg> aksjonsLoggList, Tuple... expectedEndringer) {
		assertThat(aksjonsLoggList)
			.flatExtracting(AksjonsLogg::getArkivElementEndringer)
			.extracting(ArkivElementEndring::getArkivElement, ArkivElementEndring::getFraVerdi, ArkivElementEndring::getTilVerdi)
			.containsExactlyInAnyOrder(expectedEndringer);
	}
}
