package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.skjermarkivenhet.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.skjermarkivenhet.AbstractSkjermArkivenhetIT;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Rjoark100IT extends AbstractSkjermArkivenhetIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithoutAksjon());

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
	public void skalFeileNårSkjermArkivenhetRequestMangler() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		HttpEntity httpEntity = new HttpEntity(
//				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString("Required request body is missing"));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalSkjermeJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(journalpost.getSkjermingType());

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		Optional<Journalpost> jpEtterKall = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertThat(jpEtterKall.get().getSkjermingType(), is(SkjermingTypeCode.POL));
	}


	@Test
	public void skalSkjermeDokumentInfo() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(journalpost.findHoveddokumentDokumentInfoRelasjon().getSkjermingType());

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_INFO, journalpost.getJournalpostId(),
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);

		Optional<JournalpostDokumentInfoRelasjon> jpDokInfoEtterKall = journalpostDokumentInfoRelasjonRepository.
				findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		assertTrue(jpDokInfoEtterKall.isPresent());
		assertThat(jpDokInfoEtterKall.get().getSkjermingType(), is(SkjermingTypeCode.POL));
	}

	@Test
	public void skalSkjermeDokumentFil() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name()));

		TestTransaction.start();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.POST,
				httpEntity,
				String.class);


		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertThat(dokInfoEtterKall.get()
				.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		TestTransaction.end();
	}
}
