package no.nav.dokarkiv.rjoark100;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_SKJERMING;
import static no.nav.dokarkiv.core.domain.codes.ArkivenhetCode.DOKUMENT_FIL;
import static no.nav.dokarkiv.core.domain.codes.ArkivenhetCode.DOKUMENT_INFO;
import static no.nav.dokarkiv.core.domain.codes.ArkivenhetCode.JOURNALPOST;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.security.ValidateAdminConsumerAccessInterceptor.APP_NAME_WITH_NAMESPACE;
import static no.nav.dokarkiv.core.util.TestdataFactory.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFildetaljerOgFil;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Test for skjerming av arkivenhet
 */
public class Rjoark100aIT extends AbstractAdminIT {

	@Test
	public void skalSkjermeJournalpost() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		reinitTransaction();

		assertFalse(journalpost.isSkjermet());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId)
		);

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<Journalpost> jpEtterKall = journalpostTestRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType()).isEqualTo(POL);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), ENDRE_SKJERMING,
				journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(null)
						.tilVerdi(POL.name())
						.build()
				));
	}

	@Test
	public void skalSkjermeJournalpostForStsTokenFraJoarkadmin() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		reinitTransaction();

		assertFalse(journalpost.isSkjermet());

		var headers = createHeadersWithClientCredentialAndAksjonslogg(API_ADMIN_ROLE);

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				headers
		);

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<Journalpost> jpEtterKall = journalpostTestRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType()).isEqualTo(POL);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLoggSts(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), ENDRE_SKJERMING,
				journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(null)
						.tilVerdi(POL.name())
						.build()
				));
	}

	@Test
	public void skalSkjermeDokumentInfoSomErHoveddokumentPåJournalpostSomHarVedleggRelasjoner() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSkjermes = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSkjermes = journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSkjermes);
		createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSkjermes);

		journalpostTestRepository.persist(journalpostMedDokumentSomSkalSkjermes);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		reinitTransaction();

		assertFalse(journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().isSkjermet());

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId)), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertDokumentInfoSkjermet(dokumentInfoSomSkalSkjermes.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomSkalSkjermes.getJournalpostId()), ENDRE_SKJERMING,
				journalpostMedDokumentSomSkalSkjermes.getJournalpostId(), dokumentInfoSomSkalSkjermes.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build()
				));
	}

	@Test
	public void skalSkjermeDokumentFil() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertFalse(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).isSkjermet());

		var httpEntity = new HttpEntity<>(createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType()).isEqualTo(POL);
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(PRODUKSJON).get().getSkjermingType()).isNull();


		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				singletonList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build())
		);
	}

	@Test
	public void skalSkjermeAlleFildetaljerHvisVariantIkkeErSatt() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertFalse(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).isSkjermet());
		assertFalse(dokumentInfo.findFilDetaljerByVariantFormat(PRODUKSJON).isSkjermet());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType()).isEqualTo(POL);
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(PRODUKSJON).get().getSkjermingType()).isEqualTo(POL);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build())
		);
	}


	@Test
	public void skalSkjermeDokumentFilHvisDokumentHarSladdetFildetaljer() {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SLADDET));

		journalpostTestRepository.persist(journalpost);
		assertFalse(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).isSkjermet());

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId)
		);

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).get().getSkjermingType()).isEqualTo(POL);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				singletonList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build())
		);
	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostErAlleredeSkjermet() {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();

		journalpostTestRepository.persist(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId));

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThat(skjermingService.isJournalpostSkjermet(journalpost.getJournalpostId())).isTrue();
		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), ENDRE_SKJERMING,
				journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalLageAksjonsLoggHvisJDokumentFilErAlleredeSkjermet() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS, joarkVedlikeholdGruppeId)
		);

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThat(skjermingServiceTest.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL)).isTrue();
		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalReturnereUnauthorizedHvisStsTokenIkkeErFraJoarkadmin() {
		var headers = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_IKKE_JOARKADMIN);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST,
				new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisClientCredentialToken() {
		var headers = createHeadersWithClientCredentialToken();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST,
				new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereForbiddenHvisKallendeBrukerManglerRiktigGruppe() {
		var headers = createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS);


		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST,
				new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må ha gruppen med objectId");
	}

	private void assertDokumentInfoSkjermet(Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> {
					if (rel.getTilknyttetJournalpostSom() == HOVEDDOKUMENT) {
						assertThat(skjermingService.isAllFildetaljerSkjermet(rel.getDokumentInfo())).isTrue(); // Alle Fildetaljer skal være skjermet
						assertThat(rel.getDokumentInfo().getSkjermingType()).isNull();
					} else {
						assertThat(rel.getDokumentInfo().getSkjermingType()).isEqualTo(POL);
					}
				});
	}
}
