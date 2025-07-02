package no.nav.dokarkiv.arkivervariant.rjoark103;

import no.nav.dokarkiv.arkivervariant.AbstractArkiverVariantIT;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.ArrayList;
import java.util.List;

import static no.nav.dokarkiv.arkivervariant.util.TestUtils.FIL;
import static no.nav.dokarkiv.arkivervariant.util.TestUtils.FIL2;
import static no.nav.dokarkiv.arkivervariant.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FILUUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ARKIVERING;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.security.ValidateAdminConsumerAccessInterceptor.APP_NAME_WITH_NAMESPACE;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_HJEMMEL;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_MELDING;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_UTFOERT_AV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class Rjoark103IT extends AbstractArkiverVariantIT {

	@Test
	public void shouldSaveFileAsSladdetVariant() {
		stubMsGraphMemberOfJoarkVedlikehold(MS_USER_ID_WITH_GROUP_ACCESS);
		Journalpost journalpost = journalpostTestRepository.persist(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(FIL)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build();

		var httpEntity = new HttpEntity<>(request, createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(URL_ARKIVERVARIANT, POST, httpEntity, ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		TestTransaction.start();
		assertTrue(dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId()).isPresent());
		DokumentInfo persistedDokumentInfo = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId()).get();
		assertThat(persistedDokumentInfo.getFildetaljerListeAdmin().size()).isEqualTo(2);
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(ARKIV)).isNotNull();
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(SLADDET)).isNotNull();
		assertThat(persistedDokumentInfo.findFilDetaljerByVariantFormat(SLADDET).getFiltype()).isEqualTo(PDF);
		DokumentFil dokumentFil = dokumentFilTestRepository.findByFilUuid(persistedDokumentInfo.findFilDetaljerByVariantFormat(SLADDET).getFilUuid());
		assertThat(dokumentFil.getFil()).isEqualTo(FIL);
		assertThat(responseEntity.getBody().getFilUuid()).isEqualTo(dokumentFil.getFilUuid());
		assertThat(responseEntity.getBody().getVariantFormatCode()).isEqualTo(SLADDET);
		assertThat(responseEntity.getBody().getDokumentInfoId()).isEqualTo(persistedDokumentInfo.getDokumentInfoId());

		TestTransaction.end();

		TestTransaction.start();
		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon()).isEqualTo(ARKIVERING);
		assertThat(aksjonsLogg.getUtfoertAv()).isEqualTo(AKSJON_UTFOERT_AV);
		assertThat(aksjonsLogg.getHjemmel()).isEqualTo(AKSJON_HJEMMEL);
		assertThat(aksjonsLogg.getMelding()).isEqualTo(AKSJON_MELDING);
		assertThat(aksjonsLogg.getJournalpostId()).isEqualTo(journalpost.getJournalpostId());
		assertThat(aksjonsLogg.getDokumentInfoId()).isEqualTo(dokumentInfo.getDokumentInfoId());
		assertThat(aksjonsLogg.getApplikasjon()).isEqualTo(APP_NAME_WITH_NAMESPACE);
		assertThat(aksjonsLogg.getArkivElementEndringer().size()).isEqualTo(2);

		List<ArkivElementEndring> arkivElementEndringList = new ArrayList<>(aksjonsLogg.getArkivElementEndringer());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil).toList())
				.containsExactlyInAnyOrderElementsOf(List.of(ArkivElementEndring.builder()
								.arkivElement(FILDETALJER_FILUUID)
								.fraVerdi(null)
								.tilVerdi(responseEntity.getBody().getFilUuid())
								.build().toStringElementFraTil(),
						ArkivElementEndring.builder()
								.arkivElement(FILDETALJER_VARIANTFORMAT)
								.fraVerdi(null)
								.tilVerdi(SLADDET.name())
								.build().toStringElementFraTil())

				);
		TestTransaction.end();
	}

	@Test
	public void shouldFailWithBadRequestWhenVariantAlreadyExists() {
		stubMsGraphMemberOfJoarkVedlikehold(MS_USER_ID_WITH_GROUP_ACCESS);

		Journalpost journalpost = journalpostTestRepository.persist(opprettHoveddokumentForIT());

		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(FIL)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build();

		var httpEntity = new HttpEntity<>(request, createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		ResponseEntity<ArkiverVariantResponse> responseEntity = restTemplate.exchange(URL_ARKIVERVARIANT, POST, httpEntity, ArkiverVariantResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		request = ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(FIL2)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build();

		var httpEntity2 = new HttpEntity<>(request, createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		ResponseEntity<ApplicationProblemDetail> responseEntity2 = restTemplate.exchange(
				URL_ARKIVERVARIANT,
				POST,
				httpEntity2,
				ApplicationProblemDetail.class);
		assertThat(responseEntity2.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	public void shouldFailWithNotFoundWhenDokumentInfoIsNotFound() {
		stubMsGraphMemberOfJoarkVedlikehold(MS_USER_ID_WITH_GROUP_ACCESS);

		ArkiverVariantRequest request = ArkiverVariantRequest.builder()
				.dokumentInfoId(123456L)
				.fil(FIL)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build();

		var httpEntity = new HttpEntity<>(request, createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		ResponseEntity<ApplicationProblemDetail> responseEntity = restTemplate.exchange(URL_ARKIVERVARIANT, POST, httpEntity, ApplicationProblemDetail.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	public void skalReturnereUnauthorizedHvisStsTokenIkkeErFraJoarkadmin() {
		Journalpost journalpost = journalpostTestRepository.persist(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		var headers = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_IKKE_JOARKADMIN);

		ResponseEntity<String> responseEntity =  restTemplate.exchange(URL_ARKIVERVARIANT, POST, new HttpEntity<>(ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(FIL)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisClientCredentialToken() {
		Journalpost journalpost = journalpostTestRepository.persist(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		var headers = createHeadersWithClientCredentialToken();

		ResponseEntity<String> responseEntity =  restTemplate.exchange(URL_ARKIVERVARIANT, POST, new HttpEntity<>(ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(FIL)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeBrukerManglerRiktigGruppe() {
		stubMsGraphMemberOfNotJoarkVedlikehold(MS_USER_ID_WITHOUT_GROUP_ACCESS);
		var headers = createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS);

		Journalpost journalpost = journalpostTestRepository.persist(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		ResponseEntity<String> responseEntity =  restTemplate.exchange(URL_ARKIVERVARIANT, POST, new HttpEntity<>(ArkiverVariantRequest.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.fil(FIL)
				.filnavn("filnavn")
				.variant(SLADDET)
				.filType(PDF).build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må være medlem av gruppen");
	}

}