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
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Test for opphev skjerming av arkivenhet
 */
public class Rjoark100bIT extends AbstractAdminIT {

	@Test
	public void skalOppheveSkjermingJournalpost() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);

		reinitTransaction();

		assertThat(journalpostTestRepository.findById(journalpost.getJournalpostId()).get().getSkjermingType()).isEqualTo(POL);

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		reinitTransaction();
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<Journalpost> jpEtterKall = journalpostTestRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), ENDRE_SKJERMING, journalpost
						.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(POL.name())
						.tilVerdi(null)
						.build()
				));
	}

	@Test
	public void skalOppheveSkjermingJournalpostMedClientCredentialTokenFraJoarkadmin() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);

		reinitTransaction();

		assertThat(journalpostTestRepository.findById(journalpost.getJournalpostId()).get().getSkjermingType()).isEqualTo(POL);

		var httpHeaders = createHeadersWithClientCredentialAndAksjonslogg(API_ADMIN_ROLE);

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				httpHeaders
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		reinitTransaction();
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<Journalpost> jpEtterKall = journalpostTestRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLoggSts(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), ENDRE_SKJERMING, journalpost
						.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(POL.name())
						.tilVerdi(null)
						.build()
				));
	}

	@Test
	public void skalOppheveSkjermingFraDokumentInfoSomErHoveddokumentPåEnJournalpostMedFlereVedleggRelasjoner() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomErSkjermet = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomErSkjermet = journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomErSkjermet);
		createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomErSkjermet);

		journalpostTestRepository.persist(journalpostMedDokumentSomErSkjermet);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		skjermingServiceTest.skjermAllFildetaljer(journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);

		reinitTransaction();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE,
				new HttpEntity<>(
						createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfoSomErSkjermet.getDokumentInfoId(), null),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertDokumentInfoIkkeSkjermet(dokumentInfoSomErSkjermet.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomErSkjermet.getJournalpostId()), ENDRE_SKJERMING, journalpostMedDokumentSomErSkjermet
						.getJournalpostId(), dokumentInfoSomErSkjermet.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build()
				));
	}

	@Test
	public void skalIkkeOppheveSkjermingFraDokumentInfoFildetaljerSomErHoveddokumentNårDokumentErKassert() {

		Journalpost journalpostMedDokumentSomErSkjermet = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomErSkjermet = journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		journalpostTestRepository.persist(journalpostMedDokumentSomErSkjermet);

		skjermingServiceTest.setDokumentKassert(journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);
		skjermingServiceTest.setJournalpostSkjerming(journalpostMedDokumentSomErSkjermet.getJournalpostId(), POL);
		reinitTransaction();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE,
				new HttpEntity<>(
						createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfoSomErSkjermet.getDokumentInfoId(), null),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Journalpost journalpostEtter = journalpostTestRepository.findById(journalpostMedDokumentSomErSkjermet.getJournalpostId()).get();

		assertThat(journalpostEtter.getSkjermingType()).isNull();
		assertThat(journalpostEtter.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListeAdmin()
				.stream()
				.allMatch(f -> f.getSkjermingType() != null)).isTrue();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomErSkjermet.getJournalpostId()), ENDRE_SKJERMING,
				journalpostMedDokumentSomErSkjermet.getJournalpostId(), dokumentInfoSomErSkjermet.getDokumentInfoId(),
				singletonList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build()
				));
	}

	@Test
	public void skalOppheveSkjermingDokumentInfoSomErGjenbruktSomVedleggPåEnAnnenJournalpost() {
		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		skjermingServiceTest.skjermAllFildetaljer(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);
		skjermingServiceTest.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost2, dokumentInfo.getDokumentInfoId()).getJournalpostDokumentInfoRelasjonId(), POL);
		skjermingService.setJournalpostSkjerming(originalJournalpost.getJournalpostId(), POL);
		skjermingService.setJournalpostSkjerming(journalpost1.getJournalpostId(), POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertDokumentInfoIkkeSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId()).get().getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(3);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), ENDRE_SKJERMING, originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build())
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost1.getJournalpostId()), ENDRE_SKJERMING, journalpost1
						.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(POL.name())
						.tilVerdi(null)
						.build()
				));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost2.getJournalpostId()), ENDRE_SKJERMING, journalpost2
						.getJournalpostId(), dokumentInfo
						.getDokumentInfoId(),
				singletonList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build()
				));
	}

	@Test
	public void skalOppheveSkjermingDokumentFil() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL);
		reinitTransaction();

		assertThat(dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId()).get().findFilDetaljerByVariantFormatAdmin(ARKIV).getSkjermingType()).isEqualTo(POL);

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(ARKIV).getSkjermingType());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
						.fraVerdi(POL.name())
						.tilVerdi(null)
						.build()
				));
	}

	@Test
	public void skalOppheveSkjermingDokumentFilAlleFildetaljerHvisVariantIkkeOppgitt() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), PRODUKSJON, POL);
		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(ARKIV).getSkjermingType());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(PRODUKSJON).getSkjermingType());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(PRODUKSJON))
								.fraVerdi(POL.name())
								.tilVerdi(null)
								.build()
				));
	}

	@Test
	public void skalIkkeOppheveSkjermingDokumentFilArkivOgProduksjonVariantHvisSladdetVariantEksisterer() {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SLADDET));
		journalpostTestRepository.persist(journalpost);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), PRODUKSJON, POL);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), SLADDET, POL);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(ARKIV).getSkjermingType()).isEqualTo(POL);
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(PRODUKSJON).getSkjermingType()).isEqualTo(POL);
		assertThat(dokInfoEtterKall.get().findFilDetaljerByVariantFormatAdmin(SLADDET).getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);
		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(fildetaljerSkjermingTypeVariant(SLADDET))
						.fraVerdi(POL.name())
						.tilVerdi(null)
						.build()
				));
	}

	@Test
	public void skalLageAksjonsLoggHvisDokumentInfoIkkeErSkjermet() {
		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_INFO, null, dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertDokumentInfoIkkeSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId()).get().getSkjermingType()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(3);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), ENDRE_SKJERMING, originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostIkkeErSkjermet() {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();

		journalpostTestRepository.persist(journalpost);

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS));

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(skjermingService.isJournalpostSkjermet(journalpost.getJournalpostId())).isFalse();
		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), ENDRE_SKJERMING, journalpost.getJournalpostId(),
				journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalLageAksjonsLoggHvisDokumentFilIkkeErSkjermet() {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(POL, DOKUMENT_FIL, null, dokumentInfo.getDokumentInfoId(), ARKIV),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)
		);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, httpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(skjermingServiceTest.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), ARKIV, POL)).isFalse();
		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(aksjonsLoggList.get(0), ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalReturnereUnauthorizedHvisStsTokenIkkeErFraJoarkadmin() {
		var headers = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_IKKE_JOARKADMIN);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisClientCredentialToken() {
		var headers = createHeadersWithClientCredentialToken();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeBrukerManglerRiktigGruppe() {
		var headers = createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SKJERMARKIVENHET, DELETE, new HttpEntity<>(createSkjermarkivenhetRequest(POL, JOURNALPOST, 1L, null, null), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må være medlem av gruppen");
	}

	private void assertDokumentInfoIkkeSkjermet(Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> {
					if (rel.getTilknyttetJournalpostSom() == HOVEDDOKUMENT) {
						assertThat(rel.getDokumentInfo()
								.getFildetaljerListeAdmin()
								.stream()
								.allMatch(f -> f.getSkjermingType() == null)).isTrue();
					} else {
						assertThat(rel.getSkjermingType()).isNull();
					}
				});
	}
}
