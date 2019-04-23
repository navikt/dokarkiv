package no.nav.dokarkiv.rjoark100;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithGjenbruktHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.util.TestUtil.createSkjermarkivenhetRequest;
import static no.nav.dokarkiv.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.dto.SkjermArkivenhetResponse;
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

		ResponseEntity<SkjermArkivenhetResponse> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				SkjermArkivenhetResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpEtterKall = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertCommongAksjonsLoggValues(aksjonsLogg);
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), nullValue());
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(1));

		ArkivElementEndring arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer().iterator())
				.get(0);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer().size(), is(1));
		assertThat(arkivElementEndringList.getArkivElement(), is(JOURNALPOST_SKJERMING_TYPE));
		assertThat(arkivElementEndringList.getFraVerdi(), nullValue());
		assertThat(arkivElementEndringList.getTilVerdi(), is("POL"));
		assertThat(arkivElementEndringList.getAksjonsLogg(), is(aksjonsLogg));
		TestTransaction.end();
	}


	@Test
	public void skalSkjermeDokumentInfoSomErGjenbruktOgErVedlegg() throws IOException {
		abacPermit();
		Journalpost originalJournalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfo = originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		Journalpost journalpost1 = createJournalpostWithGjenbruktHoveddokument(dokumentInfo);
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		journalpost2.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createVedleggRelasjon(journalpost2, dokumentInfo));

		joarkRepository.save(originalJournalpost);
		joarkRepository.save(journalpost1);
		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(originalJournalpost.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());
		assertNull(originalJournalpost.getSkjermingType());

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

		assertDokumentInfoSkjermet(originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), originalJournalpost
						.getJournalpostId(), dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build())
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost1.getJournalpostId()), journalpost1.getJournalpostId(), dokumentInfo
						.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_SKJERMING_TYPE)
								.fraVerdi(null)
								.tilVerdi(SkjermingTypeCode.POL.name())
								.build()
				));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost2.getJournalpostId()), journalpost2.getJournalpostId(), dokumentInfo
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

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());

		TestTransaction.start();

		restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);


		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), null, dokumentInfo.getDokumentInfoId(),
				Arrays.asList(
						ArkivElementEndring.builder()
								.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(VariantFormatCode.ARKIV))
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

		skjermingService.setJpDokInfoRelSkjerming(originalJournalpost.findHoveddokumentDokumentInfoRelasjon()
				.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost1, dokumentInfo.getDokumentInfoId()).getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJpDokInfoRelSkjerming(getRelasjonByDokumentInfoId(journalpost2, dokumentInfo.getDokumentInfoId()).getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
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

		assertDokumentInfoSkjermet(originalJournalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, originalJournalpost.getJournalpostId()), originalJournalpost
				.getJournalpostId(), dokumentInfo.getDokumentInfoId(), new ArrayList<>());

	}

	@Test
	public void skalLageAksjonsLoggHvisJournalpostErAlleredeSkjermet() throws IOException {
		Journalpost journalpost = createJournalpostWithHoveddokument();

		joarkRepository.save(journalpost);

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);

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
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), journalpost.getJournalpostId(), null,
				new ArrayList<>()
		);
	}


	@Test
	public void skalLageAksjonsLoggHvisJDokumentFilErAlleredeSkjermet() throws IOException {
		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingService.setVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);

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

		assertThat(skjermingService.isVariantSkjermet(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL), is(true));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertAksjonsLogg(aksjonsLoggList.get(0), null, dokumentInfo.getDokumentInfoId(), new ArrayList<>());
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostWithHoveddokument());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithServiceUserToken());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
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

	private void assertDokumentInfoSkjermet(Long journalpostId, Long dokumentInfoId) {
		journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)
				.forEach(rel -> assertThat(rel.getSkjermingType(), is(SkjermingTypeCode.POL)));
		assertThat(joarkRepository.findById(journalpostId).get().getSkjermingType(), is(SkjermingTypeCode.POL));
	}
}
