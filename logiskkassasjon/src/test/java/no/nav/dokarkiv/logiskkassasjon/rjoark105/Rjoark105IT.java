package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_INFO_HEADER;
import static no.nav.dokarkiv.core.util.TestDataUtils.AKSJON_LOGISK_TIDLIG_KASSASJON;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.kassereDokumentLogisk;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskkassasjon.AbstractLogiskKassasjonIT;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class Rjoark105IT extends AbstractLogiskKassasjonIT {

	@Test
	public void skalLagreAksjonVedLogiskSlett() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(0L));
		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AKSJON_LOGISK_TIDLIG_KASSASJON));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(0L));
		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_INFO_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skallIkkeLogiskKassereDokument_ettersomDokumentInfoIdIkkeFinnes() throws IOException {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("DokumentInfo ikke funnet. dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallIkkeLogiskKassereDokument_ettersomDokumentErKassert() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(kassereDokumentLogisk(dokumentInfo));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format(
				"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
				dokumentInfo.getDokumentInfoId())));
	}

	@Test
	public void skallLogiskKassereDokument_medDokumentKnyttetFlereJournalposter() throws IOException {
		abacPermit();
		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);

		joarkRepository.save(journalpost2);
		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + hoveddokument1.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(begrensningRepository.count(), is(1L));
		assertTrue(begrensningRepository.findByDokumentInfoIdAndBegrensningType(hoveddokument1.getDokumentInfoId(), BegrensningTypeCode.KASSERT)
				.isPresent());
	}

	@Test
	public void skallLogiskKassereDokument_medDokumentKnyttetEnJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(begrensningRepository.count(), is(0L));
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AKSJON_LOGISK_TIDLIG_KASSASJON)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(begrensningRepository.count(), is(1L));
		assertTrue(begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfo.getDokumentInfoId(), BegrensningTypeCode.KASSERT)
				.isPresent());
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(kassereDokumentLogisk(dokumentInfo));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_LOGISKKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccessHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
