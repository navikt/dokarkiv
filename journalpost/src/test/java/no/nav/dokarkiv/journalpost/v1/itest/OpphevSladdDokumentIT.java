package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FILUUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_PDF;

class OpphevSladdDokumentIT extends AbstractJournalpostIT {

	private static final String SLADD_DOKUMENT = "sladdDokument";
	private static final String OPPHEV_SLADD_DOKUMENT = "opphevSladdDokument";
	private static final byte[] SLADDET_FIL = "sladdet dokument".getBytes(StandardCharsets.UTF_8);

	@Test
	void skalFjerneSkjermingFraArkivOgSletteSladdetVariant() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentRelasjon.getDokumentInfo().setSkjermingType(SkjermingTypeCode.ARK);
		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = hoveddokumentRelasjon.getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		sladdDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		DokumentInfo sladdetDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		var sladdetVariantOptional = sladdetDokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET);
		assertThat(sladdetVariantOptional).isPresent();
		assertThat(sladdetDokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType())
			.isEqualTo(SkjermingTypeCode.ARK);
		String sladdetFilUuid = sladdetVariantOptional.get().getFilUuid();

		opphevSladdDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET)).isEmpty();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV).get().isSkjermet()).isFalse();

		DokumentFil slettetDokumentFil = dokumentFilTestRepository.findByFilUuid(sladdetFilUuid);
		assertThat(slettetDokumentFil).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.SLADD_DOKUMENT, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, null, dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.SLADD_DOKUMENT, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, null, dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, null, null));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(FILDETALJER_FILUUID, null, sladdetFilUuid),
			tuple(FILDETALJER_VARIANTFORMAT, null, SLADDET.name()),
			tuple(fildetaljerSkjermingTypeVariant(ARKIV), null, SkjermingTypeCode.ARK.name()),
			tuple(DOKUMENT_INFO_SKJERMING_TYPE, SkjermingTypeCode.ARK.name(), null),
			tuple(fildetaljerSkjermingTypeVariant(ARKIV), SkjermingTypeCode.ARK.name(), null),
			tuple(FILDETALJER_FILUUID, sladdetFilUuid, null),
			tuple(FILDETALJER_VARIANTFORMAT, SLADDET.name(), null),
			tuple(DOKUMENT_INFO_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()),
			tuple(JOURNALPOST_SKJERMING_TYPE, null, SkjermingTypeCode.ARK.name()));
	}

	@Test
	void skalReturnereBadRequestNaarDokumentIkkeErSladdet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentRelasjon.getDokumentInfo().setSkjermingType(SkjermingTypeCode.ARK);
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = hoveddokumentRelasjon.getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), OPPHEV_SLADD_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET)).isEmpty();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV).get().isSkjermet()).isFalse();
		assertThat(aksjonsLoggTestRepository.findAll()).isEmpty();
	}

	@Test
	void skalReturnereNotFoundNaarDokumentIkkeFinnes() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1234", OPPHEV_SLADD_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	void skalReturnereUnauthorizedNaarTokenIkkeErOboToken() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithClientCredentialToken());

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", OPPHEV_SLADD_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	void skalReturnereForbiddenNaarBrukerManglerJoarkVedlikeholdGruppe() {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITHOUT_GROUP_ACCESS));

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath("1", OPPHEV_SLADD_DOKUMENT), PATCH, requestEntity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
	}

	private void sladdDokument(Long dokumentInfoId) {
		HttpHeaders headers = createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId);
		headers.setContentType(APPLICATION_PDF);

		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SLADD_DOKUMENT), POST, new HttpEntity<>(SLADDET_FIL, headers), String.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	private void opphevSladdDokument(Long dokumentInfoId) {
		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), OPPHEV_SLADD_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
	}

	private void assertAksjonsloggEntries(List<AksjonsLogg> aksjonsLoggList, Tuple... expectedEntries) {
		assertThat(aksjonsLoggList)
			.extracting(AksjonsLogg::getAksjon, AksjonsLogg::getJournalpostId, AksjonsLogg::getDokumentInfoId, AksjonsLogg::getHjemmel)
			.containsExactly(expectedEntries);
	}

	private void assertArkivElementEndringer(List<AksjonsLogg> aksjonsLoggList, Tuple... expectedEndringer) {
		assertThat(aksjonsLoggList)
			.flatExtracting(AksjonsLogg::getArkivElementEndringer)
			.extracting(ArkivElementEndring::getArkivElement, ArkivElementEndring::getFraVerdi, ArkivElementEndring::getTilVerdi)
			.containsExactlyInAnyOrder(expectedEndringer);
	}
}
