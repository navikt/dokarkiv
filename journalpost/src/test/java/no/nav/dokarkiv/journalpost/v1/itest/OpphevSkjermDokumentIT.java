package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
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

import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.journalpost.v1.api.skjermdokument.SkjermDokumentHjemmelCode.POL;
import static org.assertj.core.api.Assertions.assertThat;
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
	void skalFjerneSkjermingFraAlleRelasjoner() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		skjermDokument(dokumentInfoId, POL);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjonerEtterSkjerming = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjonerEtterSkjerming).allMatch(r -> r.getSkjermingType() == SkjermingTypeCode.POL);

		opphevSkjermDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjonerEtterOpphev = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjonerEtterOpphev).allMatch(r -> r.getSkjermingType() == null);

		DokumentInfo dokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(dokumentInfo.getFildetaljerListeAdmin())
			.allSatisfy(filDetaljer -> {
				assertThat(filDetaljer.getSkjermingType()).isNull();
				assertThat(filDetaljer.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);
			});

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.filteredOn(AksjonsLogg::getDokumentInfoId, dokumentInfoId)
			.filteredOn(AksjonsLogg::getAksjon, AksjonsTypeCode.ENDRE_SKJERMING)
			.extracting(AksjonsLogg::getHjemmel)
			.satisfiesExactlyInAnyOrder(
				hjemmel -> assertThat(hjemmel).isNull(),
				hjemmel -> assertThat(hjemmel).isEqualTo(POL.name()));
	}

	@Test
	void skalFjerneSkjermingFraJournalpostNaarJournalpostenErSkjermet() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpostTestRepository.persist(journalpost);
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		commitAndStartNewTransaction();

		skjermDokument(dokumentInfoId, POL);

		commitAndStartNewTransaction();

		Journalpost skjermetJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(skjermetJournalpost.getSkjermingType()).isEqualTo(SkjermingTypeCode.POL);

		opphevSkjermDokument(dokumentInfoId);

		commitAndStartNewTransaction();

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSkjermingType()).isNull();

		DokumentInfo dokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(dokumentInfo.getFildetaljerListeAdmin())
			.allSatisfy(filDetaljer -> {
				assertThat(filDetaljer.getSkjermingType()).isNull();
				assertThat(filDetaljer.getEndretKildeNavn()).isEqualTo(KILDENAVN_GOSYS);
			});

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList)
			.filteredOn(AksjonsLogg::getJournalpostId, journalpostId)
			.filteredOn(AksjonsLogg::getAksjon, AksjonsTypeCode.ENDRE_SKJERMING)
			.extracting(AksjonsLogg::getHjemmel)
			.satisfiesExactlyInAnyOrder(
				hjemmel -> assertThat(hjemmel).isNull(),
				hjemmel -> assertThat(hjemmel).isEqualTo(POL.name()));
	}

	@Test
	void skalGiBadRequestNaarDokumentHarSladdetVariant() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo hoveddokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		hoveddokumentInfo.addFilDetaljer(createFildetaljerOgFil(hoveddokumentInfo, VariantFormatCode.ARKIV));
		hoveddokumentInfo.addFilDetaljer(createFildetaljerOgFil(hoveddokumentInfo, VariantFormatCode.SLADDET));
		journalpostTestRepository.persist(journalpost);
		Long dokumentInfoId = hoveddokumentInfo.getDokumentInfoId();

		commitAndStartNewTransaction();

		skjermDokument(dokumentInfoId, POL);

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(null, createHeadersWithOboToken(AZP_NAME_GOSYS, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		ResponseEntity<String> response = restTemplate.exchange(
			apiDokumentInfoPath(dokumentInfoId.toString(), OPPHEV_SKJERM_DOKUMENT), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		commitAndStartNewTransaction();

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		assertThat(relasjoner).allMatch(r -> r.getSkjermingType() == SkjermingTypeCode.POL);

		DokumentInfo dokumentInfo = dokumentInfoTestRepository.findById(dokumentInfoId).orElseThrow();
		assertThat(dokumentInfo.getFildetaljerListeAdmin())
			.allSatisfy(filDetaljer -> assertThat(filDetaljer.getSkjermingType()).isEqualTo(SkjermingTypeCode.POL));
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
}
