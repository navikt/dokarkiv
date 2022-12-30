package no.nav.dokarkiv.rjoark100;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for opphev skjerming av arkivenhet
 */
public class Rjoark100bIT extends AbstractAdminIT {

	@Test
	public void skalOppheveSkjermingJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(journalpostTestRepository.findById(journalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon());


		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpEtterKall = journalpostTestRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertNull(jpEtterKall.get().getSkjermingType());


		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
				.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build()
		));

	}

	@Test
	public void skalOppheveSkjermingFraDokumentInfoSomErHoveddokumentPåEnJournalpostMedFlereVedleggRelasjoner() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomErSkjermet = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomErSkjermet = journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomErSkjermet);
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomErSkjermet);

		journalpostTestRepository.persist(journalpostMedDokumentSomErSkjermet);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		skjermingServiceTest.skjermAllFildetaljer(journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);

		reinitTransaction();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity<>(
						createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
								dokumentInfoSomErSkjermet.getDokumentInfoId(), null),
						createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoIkkeSkjermet(dokumentInfoSomErSkjermet.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomErSkjermet.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpostMedDokumentSomErSkjermet
						.getJournalpostId(), dokumentInfoSomErSkjermet.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));
		TestTransaction.end();
	}

	@Test
	public void skalIkkeOppheveSkjermingFraDokumentInfoFildetaljerSomErHoveddokumentNårDokumentErKassert() throws IOException {
		abacPermit();


		Journalpost journalpostMedDokumentSomErSkjermet = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomErSkjermet = journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		journalpostTestRepository.persist(journalpostMedDokumentSomErSkjermet);

		skjermingServiceTest.setDokumentKassert(journalpostMedDokumentSomErSkjermet.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);
		skjermingServiceTest.setJournalpostSkjerming(journalpostMedDokumentSomErSkjermet.getJournalpostId(), SkjermingTypeCode.POL);
		reinitTransaction();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity<>(
						createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
								dokumentInfoSomErSkjermet.getDokumentInfoId(), null),
						createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Journalpost journalpostEtter = journalpostTestRepository.findById(journalpostMedDokumentSomErSkjermet.getJournalpostId()).get();

		assertThat(journalpostEtter.getSkjermingType(), nullValue());
		assertThat(journalpostEtter.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListeAdmin()
				.stream()
				.allMatch(f -> f.getSkjermingType() != null), is(true));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomErSkjermet.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpostMedDokumentSomErSkjermet
						.getJournalpostId(), dokumentInfoSomErSkjermet.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));
		TestTransaction.end();
	}


	@Test
	public void skalOppheveSkjermingDokumentInfoSomErGjenbruktSomVedleggPåEnAnnenJournalpost() throws IOException {
		abacPermit();

		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		skjermingServiceTest.skjermAllFildetaljer(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);
		skjermingServiceTest.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost2, dokumentInfo.getDokumentInfoId())
				.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJournalpostSkjerming(originalJournalpost.getJournalpostId(), SkjermingTypeCode.POL);
		skjermingService.setJournalpostSkjerming(journalpost1.getJournalpostId(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoIkkeSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId()).get().getSkjermingType(), nullValue());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
						.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build())
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost1.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost1
						.getJournalpostId(), dokumentInfo
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build()
		));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost2.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost2
						.getJournalpostId(), dokumentInfo
						.getDokumentInfoId(),
				List.of(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));

		TestTransaction.end();
	}

	@Test
	public void skalOppheveSkjermingDokumentFil() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		reinitTransaction();

		assertThat(dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId())
				.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), List.of(
				ArkivElementEndring.builder()
						.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build()
		));
	}

	@Test
	public void skalOppheveSkjermingDokumentFilAlleFildetaljerHvisVariantIkkeOppgitt() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.PRODUKSJON, SkjermingTypeCode.POL);
		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON).getSkjermingType());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), Arrays
				.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));
	}

	@Test
	public void skalIkkeOppheveSkjermingDokumentFilArkivOgProduksjonVariantHvisSladdetVariantEksisterer() throws IOException {
		abacPermit();

		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.SLADDET));
		journalpostTestRepository.persist(journalpost);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.PRODUKSJON, SkjermingTypeCode.POL);
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.SLADDET, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.PRODUKSJON)
				.getSkjermingType(), is(SkjermingTypeCode.POL));
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.SLADDET)
				.getSkjermingType(), nullValue());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.SLADDET))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));
	}
	
	@Test
	public void skalLageAksjonsLoggHvisDokumentInfoIkkeErSkjermet() throws IOException {
		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertDokumentInfoIkkeSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId()).get().getSkjermingType(), nullValue());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
				.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostIkkeErSkjermet() throws IOException {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();

		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(),
						null, null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(skjermingService.isJournalpostSkjermet(journalpost.getJournalpostId()), is(false));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
				.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalLageAksjonsLoggHvisDokumentFilIkkeErSkjermet() throws IOException {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(skjermingServiceTest.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL), is(false));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalIkkeFåTilgangHvisServiceBrukerIkkeErSrvJoarkadmin() {

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, 1L, null, null),
				createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID));


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	private void assertDokumentInfoIkkeSkjermet(Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> {
					if (rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.HOVEDDOKUMENT) {
						assertThat(rel.getDokumentInfo()
								.getFildetaljerListeAdmin()
								.stream()
								.allMatch(f -> f.getSkjermingType() == null), is(true));
					} else {
						assertThat(rel.getSkjermingType(), nullValue());
					}
				});
	}
}
