package no.nav.dokarkiv.rjoark100;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static no.nav.dokarkiv.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

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
 * Test for skjerming av arkivenhet
 */
public class Rjoark100aIT extends AbstractAdminIT {

	@Test
	public void skalSkjermeJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostWithHoveddokument());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(journalpost.getSkjermingType());

		TestTransaction.start();
		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon());

		ResponseEntity responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpEtterKall = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(2));

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), journalpost
						.findHoveddokumentDokumentInfoRelasjon()
						.getDokumentInfo()
						.getDokumentInfoId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
						.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
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

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), null), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
						.getJournalpostId(), null,
				Arrays.asList(
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

		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSkjermes = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSkjermes = journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSkjermes);
		TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSkjermes);

		joarkRepository.save(journalpostMedDokumentSomSkalSkjermes);
		joarkRepository.save(journalpost1);
		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(journalpostMedDokumentSomSkalSkjermes.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				new HttpEntity(
						createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
								dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
						createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoSkjermet(dokumentInfoSomSkalSkjermes.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
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
		Journalpost originalJournalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSkjermes = originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();

		Journalpost journalpostMedHoveddokumentSomErGjenbrukt = createJournalpostWithGjenbruktHoveddokument(dokumentInfoSomSkalSkjermes);
		Journalpost journalpostMedHoveddokuemtn = createJournalpostWithHoveddokument();
		journalpostMedHoveddokuemtn.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpostMedHoveddokuemtn, dokumentInfoSomSkalSkjermes));

		joarkRepository.save(originalJournalpost);
		joarkRepository.save(journalpostMedHoveddokumentSomErGjenbrukt);
		joarkRepository.save(journalpostMedHoveddokuemtn);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());
		assertNull(originalJournalpost.getSkjermingType());

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
						dokumentInfoSomSkalSkjermes.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoSkjermet(dokumentInfoSomSkalSkjermes.getDokumentInfoId());
		assertThat(joarkRepository.findById(originalJournalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
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
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedHoveddokuemtn.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpostMedHoveddokuemtn
						.getJournalpostId(), dokumentInfoSomSkalSkjermes
						.getDokumentInfoId(),
				Arrays.asList(
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

		Journalpost journalpost = joarkRepository.save(createJournalpostWithHoveddokument());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		reinitTransaction();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		ResponseEntity responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(VariantFormatCode.ARKIV))
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build())
		);
		TestTransaction.end();
	}

	@Test
	public void skalSkjermeDokumentFilHvisDokumentHarSladdetFildetaljer() throws IOException {
		abacPermit();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.SLADDET));

		joarkRepository.save(journalpost);
		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		reinitTransaction();

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		ResponseEntity responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
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
		Journalpost originalJournalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		joarkRepository.save(originalJournalpost);
		joarkRepository.save(journalpost1);
		joarkRepository.save(journalpost2);

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

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, null,
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertDokumentInfoSkjermet(dokumentInfo.getDokumentInfoId());
		assertThat(joarkRepository.findById(originalJournalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), AksjonsTypeCode.ENDRE_SKJERMING, originalJournalpost
				.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());

	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostErAlleredeSkjermet() throws IOException {
		Journalpost journalpost = createJournalpostWithHoveddokument();

		joarkRepository.save(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(),
						null, null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(skjermingService.isJournalpostSkjermet(journalpost.getJournalpostId()), is(true));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(2));

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), journalpost
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
				.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId(), new ArrayList<>());
		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), null), AksjonsTypeCode.ENDRE_SKJERMING, journalpost
				.getJournalpostId(), null, new ArrayList<>());
	}


	@Test
	public void skalLageAksjonsLoggHvisJDokumentFilErAlleredeSkjermet() throws IOException {
		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingServiceTest.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThat(skjermingServiceTest.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL), is(true));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.ENDRE_SKJERMING, journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalIkkeFåTilgangHvisServiceBrukerIkkeErSrvJoarkadmin() {

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, 1L, null, null),
				createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	private void assertDokumentInfoSkjermet(Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
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
