package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentHjemmelCode;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class AngreSkjermDokumentIT extends AbstractJournalpostIT {

	private static final String SKJERM_DOKUMENT = "skjermDokument";
	private static final String ANGRE_SKJERM_DOKUMENT = "skjermDokument/angre";

	@Test
	void skalFjerneSkjermingFraAlleRelasjoner() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		skjermDokument(dokumentInfoId, SkjermDokumentHjemmelCode.POL);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjonerEtterSkjerming = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjonerEtterSkjerming).allMatch(r -> r.getSkjermingType() == SkjermingTypeCode.POL);

		angreSkjermDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjonerEtterAngre = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjonerEtterAngre).allMatch(r -> r.getSkjermingType() == null);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.anyMatch(al -> dokumentInfoId.equals(al.getDokumentInfoId())
				&& al.getAksjon() == AksjonsTypeCode.ENDRE_SKJERMING
				&& al.getHjemmel() == null);
	}

	@Test
	void skalFjerneSkjermingFraJournalpostNaarJournalpostenErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		skjermDokument(dokumentInfoId, SkjermDokumentHjemmelCode.POL);

		commitAndStartNewTransaction();

		Journalpost skjermetJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(skjermetJournalpost.getSkjermingType()).isEqualTo(SkjermingTypeCode.POL);

		angreSkjermDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.anyMatch(al -> journalpostId.equals(al.getJournalpostId())
				&& al.getAksjon() == AksjonsTypeCode.ENDRE_SKJERMING
				&& al.getHjemmel() == null);
	}

	@Test
	void skalGiBadRequestNarDokumentIkkeErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), ANGRE_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjoner).allMatch(r -> r.getSkjermingType() == null);
	}

	@Test
	void skalReturnereNotFoundNaarDokumentIkkeFinnes() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1234", ANGRE_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	void skalReturnereUnauthorizedNaarTokenIkkeErOboToken() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithClientCredentialToken());

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", ANGRE_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	void skalReturnereForbiddenNaarBrukerManglerJoarkVedlikeholdGruppe() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITHOUT_GROUP_ACCESS));

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", ANGRE_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
	}

	private void skjermDokument(Long dokumentInfoId, SkjermDokumentHjemmelCode hjemmel) {
		var request = new SkjermDokumentRequest(hjemmel);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	private ResponseEntity<String> angreSkjermDokument(Long dokumentInfoId) {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), ANGRE_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
		return response;
	}
}
