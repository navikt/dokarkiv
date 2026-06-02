package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentHjemmelCode;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class SkjermDokumentIT extends AbstractJournalpostIT {

	private static final String SKJERM_DOKUMENT = "skjermDokument";

	@Test
	void skalAvviseRequestMedHjemmelPOLMedBadRequest() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.POL);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjoner)
			.allMatch(r -> r.getSkjermingType() == null);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isEmpty();
	}

	@Test
	void skalSkjermeRelasjonerOgLoggeAksjonsloggForARK() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjoner)
			.allMatch(r -> r.getSkjermingType() == SkjermingTypeCode.ARK)
			.extracting(JournalpostDokumentInfoRelasjon::getEndretKildeNavn)
			.allSatisfy(endretKildeNavn -> assertThat(endretKildeNavn).isEqualTo(KILDENAVN_GOSYS));

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.filteredOn(AksjonsLogg::getDokumentInfoId, dokumentInfoId)
			.filteredOn(AksjonsLogg::getAksjon, AksjonsTypeCode.ENDRE_SKJERMING)
			.extracting(AksjonsLogg::getHjemmel)
			.satisfiesExactlyInAnyOrder(
				hjemmel -> assertThat(hjemmel).isEqualTo(SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalOverskriveSkjermingOmDokumentErSkjermetMenNyVerdiIRequest() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		var requestEntity2 = new HttpEntity<>(new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK), createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response2 = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity2, String.class);

		assertThat(response2.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjoner).allMatch(r -> r.getSkjermingType() == SkjermingTypeCode.ARK);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.filteredOn(AksjonsLogg::getDokumentInfoId, dokumentInfoId)
			.filteredOn(AksjonsLogg::getAksjon, AksjonsTypeCode.ENDRE_SKJERMING)
			.extracting(AksjonsLogg::getHjemmel)
			.satisfiesExactlyInAnyOrder(
				hjemmel -> assertThat(hjemmel).isEqualTo(SkjermingTypeCode.ARK.name()),
				hjemmel -> assertThat(hjemmel).isEqualTo(SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalSkjermeJournalpostNaarAlleRelasjonerErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();

		JournalpostDokumentInfoRelasjon vedleggRelasjon = createDokumentInfoVedleggRelasjon(journalpost);
		vedleggRelasjon.setSkjermingType(SkjermingTypeCode.ARK);

		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);
		assertThat(oppdatertJournalpost.getEndretAvNavn()).isEqualTo("F_990782 E_990782");
		assertThat(oppdatertJournalpost.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);

		assertThat(oppdatertJournalpost.getJournalpostDokumentInfoRelasjonerAdmin())
			.filteredOn(JournalpostDokumentInfoRelasjon::isHoveddokument)
			.extracting(JournalpostDokumentInfoRelasjon::getEndretKildeNavn)
			.satisfiesExactlyInAnyOrder(endretKildeNavn -> assertThat(endretKildeNavn).isEqualTo(KILDENAVN_GOSYS));

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.filteredOn(AksjonsLogg::getDokumentInfoId, dokumentInfoId)
			.filteredOn(AksjonsLogg::getAksjon, AksjonsTypeCode.ENDRE_SKJERMING)
			.extracting(AksjonsLogg::getHjemmel)
			.satisfiesExactlyInAnyOrder(
				hjemmel -> assertThat(hjemmel).isEqualTo(SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalIkkeSkjermeJournalpostNaarIkkeAlleRelasjonerErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();

		createDokumentInfoVedleggRelasjon(journalpost);

		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();

		assertThat(oppdatertJournalpost.getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		assertThat(aksjonsLoggList).hasSize(1);
		assertThat(aksjonsLoggList.getFirst().getDokumentInfoId()).isEqualTo(dokumentInfoId);
	}

	@Test
	void skalSkjermeDokumentSomErKnyttetTilFlereJournalposter() {
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost1);
		DokumentInfo deldDokumentInfo = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		Long dokumentInfoId = deldDokumentInfo.getDokumentInfoId();

		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		journalpost2.setKanalReferanseId("KANAL_REFERANSE_ID_2");

		JournalpostDokumentInfoRelasjon vedleggRelasjon = no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon(journalpost2, deldDokumentInfo);
		journalpostTestRepository.persist(journalpost2);

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjoner)
			.hasSize(2)
			.allSatisfy(r -> assertThat(r.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK));
	}

	@Test
	void skalReturnereUnauthorizedNaarTokenIkkeErOboToken() {
		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithClientCredentialToken());

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	void skalReturnereForbiddenNarBrukerManglerJoarkVedlikeholdGruppe() {
		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITHOUT_GROUP_ACCESS));

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
	}

	@Test
	void skalReturnereNotForundNarDokumentIkkeFinnnes() {
		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1234", SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}
}
