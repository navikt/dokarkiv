package no.nav.dokarkiv.rjoark100;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
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
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Test for skjerming av arkivenhet
 */
public class Rjoark100aIT extends AbstractAdminIT {

	@Test
	public void skalSkjermeJournalpost() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		reinitTransaction();

		assertNull(journalpost.getSkjermingType());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
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
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		reinitTransaction();

		assertNull(journalpost.getSkjermingType());

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
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
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

		assertNull(journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)), String.class);

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

	/**
	 * Case
	 * <p>
	 * OrigJp -> dokumentSomSkalSkjermes(hoveddok)
	 * JP1 -> dokumentSomSkalSkjermes(hoveddok)
	 * JP2 -> dokumentSomSkalSkjermes(vedlegg)
	 * -> dokument(hoveddok)
	 */
	@Test
	public void skalSkjermeDokumentInfoSomErGjenbruktSomHoveddokumentPåEnJournalpostOgSomVedleggPåEnAnnen() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSkjermes = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpostMedHoveddokumentSomErGjenbrukt = createJournalpostWithGjenbruktHoveddokument(dokumentInfoSomSkalSkjermes);
		Journalpost journalpostMedHoveddokument = createUniqueJournalpostWithHoveddokument();
		journalpostMedHoveddokument.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpostMedHoveddokument, dokumentInfoSomSkalSkjermes));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpostMedHoveddokumentSomErGjenbrukt);
		journalpostTestRepository.persist(journalpostMedHoveddokument);

		reinitTransaction();

		assertNull(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());
		assertNull(originalJournalpost.getSkjermingType());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertDokumentInfoSkjermet(dokumentInfoSomSkalSkjermes.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId()).get().getSkjermingType()).isEqualTo(POL);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(3);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), ENDRE_SKJERMING,
				originalJournalpost.getJournalpostId(), dokumentInfoSomSkalSkjermes.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build(),
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
				)
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedHoveddokumentSomErGjenbrukt.getJournalpostId()), ENDRE_SKJERMING,
				journalpostMedHoveddokumentSomErGjenbrukt.getJournalpostId(), dokumentInfoSomSkalSkjermes.getDokumentInfoId(),
				singletonList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build()
				));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedHoveddokument.getJournalpostId()), ENDRE_SKJERMING,
				journalpostMedHoveddokument.getJournalpostId(), dokumentInfoSomSkalSkjermes.getDokumentInfoId(),
				singletonList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(POL.name())
								.build()
				));
	}

	@Test
	public void skalSkjermeDokumentFil() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).getSkjermingType());

		var httpEntity = new HttpEntity<>(createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).getSkjermingType()).isEqualTo(POL);
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(PRODUKSJON).getSkjermingType()).isNull();


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
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).getSkjermingType());
		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(PRODUKSJON).getSkjermingType());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).getSkjermingType()).isEqualTo(POL);
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(PRODUKSJON).getSkjermingType()).isEqualTo(POL);

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
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SLADDET));

		journalpostTestRepository.persist(journalpost);
		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).getSkjermingType());

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).getSkjermingType()).isEqualTo(POL);

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
	public void skalLageAksjonsLoggHvisDokumentInfoErAlleredeSkjermet() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		skjermingServiceTest.skjermAllFildetaljer(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);
		skjermingServiceTest.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost2, dokumentInfo.getDokumentInfoId()).getJournalpostDokumentInfoRelasjonId(), POL);
		skjermingService.setJournalpostSkjerming(originalJournalpost.getJournalpostId(), POL);
		skjermingService.setJournalpostSkjerming(journalpost1.getJournalpostId(), POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		var responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertDokumentInfoSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId()).get().getSkjermingType()).isEqualTo(POL);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(3);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), ENDRE_SKJERMING,
				originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostErAlleredeSkjermet() {
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();

		journalpostTestRepository.persist(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

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
		stubMsGraphMemberOfJoarkVelikehold(MS_ID_SAKSBEHANDLER);
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
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
	public void skalReturnereUnauthorizedHvisKallendeBrukerManglerRiktigGruppe() {
		stubMsGraphMemberOfNotJoarkVelikeholdAdmin(MS_ID_SAKSBEHANDLER);
		var headers = createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS);


		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, POST,
				new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må være medlem av gruppen");
	}

	private void assertDokumentInfoSkjermet(Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> {
					if (rel.getTilknyttetJournalpostSom() == HOVEDDOKUMENT) {
						assertThat(skjermingService.isAllFildetaljerSkjermet(rel.getDokumentInfo())).isTrue(); // Alle Fildetaljer skal være skjermet
						assertThat(rel.getSkjermingType()).isNull();
					} else {
						assertThat(rel.getSkjermingType()).isEqualTo(POL);
					}
				});
	}
}
