package no.nav.dokarkiv.journalpost.v1.itest;

import java.util.List;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentHjemmelCode;
import no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentRequest;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.util.TestdataFactory.createDokumentInfoVedleggRelasjonForJournalpost;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFerdigstiltJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestdataFactory.createVedleggRelasjon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
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
		Journalpost journalpost = createFerdigstiltJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.POL);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.isSkjermet()).isFalse();
		assertThat(oppdatertDokumentInfo.getEndretKildeNavn()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isEmpty();
	}

	@Test
	void skalSkjermeRelasjonerOgLoggeAksjonsloggForARK() {
		Journalpost journalpost = createFerdigstiltJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var request = new SkjermDokumentRequest(SkjermDokumentHjemmelCode.ARK);
		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SKJERM_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);
		assertThat(oppdatertDokumentInfo.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalOverskriveSkjermingOmDokumentErSkjermetMenNyVerdiIRequest() {
		Journalpost journalpost = createFerdigstiltJournalpostWithHoveddokument();
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

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);
		assertThat(oppdatertDokumentInfo.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()),
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalSkjermeJournalpostNaarAlleRelasjonerErSkjermet() {
		Journalpost journalpost = createFerdigstiltJournalpostWithHoveddokument();

		JournalpostDokumentInfoRelasjon vedleggRelasjon = createDokumentInfoVedleggRelasjonForJournalpost(journalpost);
		vedleggRelasjon.getDokumentInfo().setSkjermingType(SkjermingTypeCode.ARK);

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

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);
		assertThat(oppdatertDokumentInfo.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalIkkeSkjermeJournalpostNaarIkkeAlleRelasjonerErSkjermet() {
		Journalpost journalpost = createFerdigstiltJournalpostWithHoveddokument();

		createDokumentInfoVedleggRelasjonForJournalpost(journalpost);

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

		assertThat(oppdatertJournalpost.isSkjermet()).isFalse();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);
		assertThat(oppdatertDokumentInfo.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalSkjermeDokumentSomErKnyttetTilFlereJournalposter() {
		Journalpost journalpost1 = createFerdigstiltJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost1);
		DokumentInfo deldDokumentInfo = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		Long dokumentInfoId = deldDokumentInfo.getDokumentInfoId();

		Journalpost journalpost2 = createFerdigstiltJournalpostWithHoveddokument();
		journalpost2.setKanalReferanseId("KANAL_REFERANSE_ID_2");

		JournalpostDokumentInfoRelasjon vedleggRelasjon = createVedleggRelasjon(journalpost2, deldDokumentInfo);
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
			.hasSize(2);

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.getSkjermingType()).isEqualTo(SkjermingTypeCode.ARK);
		assertThat(oppdatertDokumentInfo.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost1.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost2.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()),
			tuple(RELASJON_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()));
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
