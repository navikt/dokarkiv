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
 * Test for skjerming av arkivenhet
 */
public class Rjoark100aIT extends AbstractAdminIT {

	@Test
	public void skalSkjermeJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(journalpost.getSkjermingType());

		TestTransaction.start();
		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon());

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpEtterKall = journalpostTestRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
						.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				List.of(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				));

		TestTransaction.end();
	}

	@Test
	public void skalSkjermeDokumentInfoSomErHoveddokumentPåJournalpostSomHarVedleggRelasjoner() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSkjermes = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSkjermes = journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSkjermes);
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSkjermes);

		journalpostTestRepository.persist(journalpostMedDokumentSomSkalSkjermes);
		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());

		TestTransaction.start();
		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				new HttpEntity<>(
						createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
								dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
						createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoSkjermet(dokumentInfoSomSkalSkjermes.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomSkalSkjermes.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpostMedDokumentSomSkalSkjermes
						.getJournalpostId(), dokumentInfoSomSkalSkjermes.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				));
		TestTransaction.end();
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
	public void skalSkjermeDokumentInfoSomErGjenbruktSomHoveddokumentPåEnJournalpostOgSomVedleggPåEnAnnen() throws IOException {
		abacPermit();
		Journalpost originalJournalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSkjermes = originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		Journalpost journalpostMedHoveddokumentSomErGjenbrukt = createJournalpostWithGjenbruktHoveddokument(dokumentInfoSomSkalSkjermes);
		Journalpost journalpostMedHoveddokument = createUniqueJournalpostWithHoveddokument();
		journalpostMedHoveddokument.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpostMedHoveddokument, dokumentInfoSomSkalSkjermes));

		journalpostTestRepository.persist(originalJournalpost);
		journalpostTestRepository.persist(journalpostMedHoveddokumentSomErGjenbrukt);
		journalpostTestRepository.persist(journalpostMedHoveddokument);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());
		assertNull(originalJournalpost.getSkjermingType());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
						dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		TestTransaction.start();
		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoSkjermet(dokumentInfoSomSkalSkjermes.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
						.getJournalpostId(), dokumentInfoSomSkalSkjermes.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				)
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedHoveddokumentSomErGjenbrukt.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpostMedHoveddokumentSomErGjenbrukt
						.getJournalpostId(), dokumentInfoSomSkalSkjermes
						.getDokumentInfoId(),
				List.of(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedHoveddokument.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpostMedHoveddokument
						.getJournalpostId(), dokumentInfoSomSkalSkjermes
						.getDokumentInfoId(),
				List.of(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				));

		TestTransaction.end();
	}

	@Test
	public void skalSkjermeDokumentFil() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
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
				.getSkjermingType(), nullValue());


		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				List.of(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build())
		);
		TestTransaction.end();
	}

	@Test
	public void skalSkjermeAlleFildetaljerHvisVariantIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());
		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON).getSkjermingType());

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
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

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.PRODUKSJON))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build())
		);
		TestTransaction.end();
	}


	@Test
	public void skalSkjermeDokumentFilHvisDokumentHarSladdetFildetaljer() throws IOException {
		abacPermit();

		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.SLADDET));

		journalpostTestRepository.persist(journalpost);
		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		reinitTransaction();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentInfoTestRepository.findById(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				List.of(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build())
		);
		TestTransaction.end();
	}

	@Test
	public void skalLageAksjonsLoggHvisDokumentInfoErAlleredeSkjermet() throws IOException {
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
		skjermingServiceTest.skjermAllFildetaljer(journalpost1.findHoveddokumentDokumentInfoRelasjon()
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
		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(journalpostTestRepository.findById(originalJournalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
				.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());

	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostErAlleredeSkjermet() throws IOException {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();

		journalpostTestRepository.persist(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(),
						null, null),
				createHeadersWithAksjon());

		TestTransaction.start();
		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(skjermingService.isJournalpostSkjermet(journalpost.getJournalpostId()), is(true));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
				.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), new ArrayList<>());
	}


	@Test
	public void skalLageAksjonsLoggHvisJDokumentFilErAlleredeSkjermet() throws IOException {
		Journalpost journalpost = journalpostTestRepository.persist(createUniqueJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		TestTransaction.start();
		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(skjermingServiceTest.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL), is(true));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggTestRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalIkkeFåTilgangHvisServiceBrukerIkkeErSrvJoarkadmin() {

		var httpEntity = new HttpEntity<>(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, 1L, null, null),
				createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID));

		var responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	private void assertDokumentInfoSkjermet(Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> {
					if (rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.HOVEDDOKUMENT) {
						assertThat("Alle Fildetaljer skal være skjermet", skjermingService.isAllFildetaljerSkjermet(rel.getDokumentInfo()), is(true));
						assertThat(rel.getSkjermingType(), nullValue());
					} else {
						assertThat(rel.getSkjermingType(), is(SkjermingTypeCode.POL));
					}
				});
	}
}
