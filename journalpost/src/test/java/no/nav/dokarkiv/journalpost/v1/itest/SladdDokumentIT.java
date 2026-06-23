package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
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
import java.util.Optional;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FILUUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_PDF;

class SladdDokumentIT extends AbstractJournalpostIT {

	private static final String SLADD_DOKUMENT = "sladdDokument";
	private static final byte[] SLADDET_FIL = "sladdet dokument".getBytes(StandardCharsets.UTF_8);

	@Test
	void skalAvviseSladdingAvUskjermetDokumentMedBadRequest() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		ResponseEntity<String> response = sladdDokument(dokumentInfoId, SLADDET_FIL, oboHeadersPdf());

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET)).isEmpty();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType()).isNull();

		assertThat(aksjonsLoggTestRepository.findAll()).isEmpty();
	}

	@Test
	void skalOppretteSladdetVariantOgSkjermeArkivMedSkjermingFraRelasjon() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentRelasjon.setSkjermingType(SkjermingTypeCode.ARK);
		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = hoveddokumentRelasjon.getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		ResponseEntity<String> response = sladdDokument(dokumentInfoId, SLADDET_FIL, oboHeadersPdf());

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();

		Optional<FilDetaljer> sladdetVariantOptional = oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET);
		assertThat(sladdetVariantOptional).isPresent();
		FilDetaljer sladdetVariant = sladdetVariantOptional.get();
		assertThat(sladdetVariant.getFiltype()).isEqualTo(FilTypeCode.PDF);

		DokumentFil sladdetDokumentFil = dokumentFilTestRepository.findByFilUuid(sladdetVariant.getFilUuid());
		assertThat(sladdetDokumentFil).isNotNull();
		assertThat(sladdetDokumentFil.getFil()).isEqualTo(SLADDET_FIL);

		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType())
			.isEqualTo(SkjermingTypeCode.ARK);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.SLADD_DOKUMENT, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId, SkjermingTypeCode.ARK.name()));
		assertArkivElementEndringer(aksjonsLoggList,
			tuple(FILDETALJER_FILUUID, null, sladdetVariant.getFilUuid()),
			tuple(FILDETALJER_VARIANTFORMAT, null, SLADDET.name()),
			tuple(fildetaljerSkjermingTypeVariant(ARKIV), null, SkjermingTypeCode.ARK.name()),
			tuple(RELASJON_SKJERMING_TYPE, SkjermingTypeCode.ARK.name(), null));
	}

	@Test
	void skalSladdeDokumentSomErKnyttetTilFlereJournalposter() {
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = journalpost1.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentRelasjon.setSkjermingType(SkjermingTypeCode.ARK);
		journalpostTestRepository.persist(journalpost1);
		DokumentInfo deltDokumentInfo = hoveddokumentRelasjon.getDokumentInfo();
		Long dokumentInfoId = deltDokumentInfo.getDokumentInfoId();

		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		journalpost2.setKanalReferanseId("KANAL_REFERANSE_ID_2");
		JournalpostDokumentInfoRelasjon vedleggRelasjon = no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon(journalpost2, deltDokumentInfo);
		vedleggRelasjon.setSkjermingType(SkjermingTypeCode.ARK);
		journalpostTestRepository.persist(journalpost2);

		commitAndStartNewTransaction();

		ResponseEntity<String> response = sladdDokument(dokumentInfoId, SLADDET_FIL, oboHeadersPdf());

		assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);

		commitAndStartNewTransaction();

		DokumentInfo oppdatertDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		var sladdetVariantOptional = oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET);
		assertThat(sladdetVariantOptional).isPresent();
		assertThat(oppdatertDokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType())
			.isEqualTo(SkjermingTypeCode.ARK);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertAksjonsloggEntries(aksjonsLoggList,
			tuple(AksjonsTypeCode.SLADD_DOKUMENT, journalpost1.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.SLADD_DOKUMENT, journalpost2.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost1.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name()),
			tuple(AksjonsTypeCode.ENDRE_SKJERMING, journalpost2.getJournalpostId(), dokumentInfoId, SkjermingTypeCode.ARK.name())
		);
	}

	@Test
	void skalReturnereUnauthorizedNaarTokenIkkeErOboToken() {
		HttpHeaders headers = createHeadersWithClientCredentialToken();
		headers.setContentType(APPLICATION_PDF);

		ResponseEntity<String> response = sladdDokument(1L, SLADDET_FIL, headers);

		assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	void skalReturnereForbiddenNarBrukerManglerJoarkVedlikeholdGruppe() {
		HttpHeaders headers = createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITHOUT_GROUP_ACCESS);
		headers.setContentType(APPLICATION_PDF);

		ResponseEntity<String> response = sladdDokument(1L, SLADDET_FIL, headers);

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
	}

	@Test
	void skalReturnereNotFoundNaarDokumentIkkeFinnes() {
		ResponseEntity<String> response = sladdDokument(1234L, SLADDET_FIL, oboHeadersPdf());

		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	private HttpHeaders oboHeadersPdf() {
		HttpHeaders headers = createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId);
		headers.setContentType(APPLICATION_PDF);
		return headers;
	}

	private ResponseEntity<String> sladdDokument(Long dokumentInfoId, byte[] fil, HttpHeaders headers) {
		return restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), SLADD_DOKUMENT), POST, new HttpEntity<>(fil, headers), String.class);
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
