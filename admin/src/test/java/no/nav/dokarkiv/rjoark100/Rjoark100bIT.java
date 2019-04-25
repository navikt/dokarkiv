package no.nav.dokarkiv.rjoark100;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static no.nav.dokarkiv.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
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

/**
 * Test for opphev skjerming av arkivenhet
 */
public class Rjoark100bIT extends AbstractAdminIT {

	@Test
	public void skalOppheveSkjermingJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(joarkRepository.findById(journalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon());


		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpEtterKall = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertNull(jpEtterKall.get().getSkjermingType());


		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), null, Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build()
		));
	}


	@Test
	public void skalOppheveSkjermingDokumentInfo() throws IOException {
		abacPermit();

		Journalpost originalJournalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		joarkRepository.save(originalJournalpost);
		joarkRepository.save(journalpost1);
		joarkRepository.save(journalpost2);

		skjermingService.setJpDokInfoRelSkjerming(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost1, dokumentInfo.getDokumentInfoId()).getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost2, dokumentInfo.getDokumentInfoId()).getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJournalpostSkjerming(originalJournalpost.getJournalpostId(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(
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

		assertDokumentInfoIkkeSkjermet(originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
						.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
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
						.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost2.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost2
						.getJournalpostId(), dokumentInfo
						.getDokumentInfoId(),
				Arrays.asList(
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

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingService.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);


		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, null, dokumentInfo.getDokumentInfoId(), Arrays
				.asList(
						ArkivElementEndring.builder()
								.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(VariantFormatCode.ARKIV))
								.fraVerdi(SkjermingTypeCode.POL.name())
								.tilVerdi(null)
								.build()
				));
	}


	@Test
	public void skalLageAksjonsLoggHvisDokumentInfoIkkeErSkjermet() throws IOException {
		Journalpost originalJournalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		joarkRepository.save(originalJournalpost);
		joarkRepository.save(journalpost1);
		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(
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
		assertDokumentInfoIkkeSkjermet(originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
				.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());

	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostIkkeErSkjermet() throws IOException {
		Journalpost journalpost = createJournalpostWithHoveddokument();

		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(
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

		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), null, new ArrayList<>());

	}

	@Test
	public void skalLageAksjonsLoggHvisDokumentFilIkkeErSkjermet() throws IOException {
		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(
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
		assertThat(skjermingService.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL), is(false));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, null, dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalIkkeFåTilgangHvisServiceBrukerIkkeErSrvJoarkadmin() {

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, 1L, null, null),
				createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID));


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	private void assertDokumentInfoIkkeSkjermet(Long journalpostId, Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> assertThat(rel.getSkjermingType(), nullValue()));
		assertThat(joarkRepository.findById(journalpostId).get().getSkjermingType(), nullValue());
	}


}
