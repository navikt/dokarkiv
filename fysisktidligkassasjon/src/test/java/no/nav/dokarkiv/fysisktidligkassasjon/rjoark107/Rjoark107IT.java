package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SLETT;
import static no.nav.dokarkiv.fysisktidligkassasjon.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.fysisktidligkassasjon.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.fysisktidligkassasjon.AbstractFysiskTidligKassasjonIT;
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

public class Rjoark107IT extends AbstractFysiskTidligKassasjonIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();
		DokumentInfo dokumentInfo1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo1, journalpost2);

		joarkRepository.save(journalpost2);
		skjermingService.setDokumentKassert(dokumentInfo1, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo1.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoRep.get()));

		assertThat("Feil antall journalposter", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(2L));
		assertTrue(dokumentInfo1.isRelatedToMultipleJournalposts());

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfo1.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(SLETT));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();
		DokumentInfo dokumentInfo1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo1, journalpost2);

		joarkRepository.save(journalpost2);
		skjermingService.setDokumentKassert(dokumentInfo1, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfo1.getDokumentInfoId(),
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

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
				URL_FYSISKTIDLIGKASSASJON + dokumentInfoId,
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skallIkkeTidligtKassereDokument_ettersomDokmentInfoIdIkkeFinnes() throws IOException {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfoId,
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Kan ikke finne dokument med dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallIkkeTidligtKassereDokument_ettersomDokmentInfoIkkeErLogiskKassert() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(String.format(
				"Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
				dokumentInfo.getDokumentInfoId(),
				SkjermingTypeCode.POL)));
	}

	@Test
	public void skallTidligtKassereDokument_medDokmentKnyttetFlereJournalposter() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();
		DokumentInfo dokumentInfo1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo1, journalpost2);

		joarkRepository.save(journalpost2);

		skjermingService.setDokumentKassert(dokumentInfo1, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo1.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoRep.get()));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(2L));
		assertTrue(dokumentInfo1.isRelatedToMultipleJournalposts());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfo1.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo1.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoRep.get()));

		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(2L));
	}

	@Test
	public void skallTidligtKassereDokument_medDokumentKnyttetEnJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		skjermingService.setDokumentKassert(dokumentInfo, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoRep.get()));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(1L));
		assertFalse(dokumentInfo.isRelatedToMultipleJournalposts());
		assertFalse(dokumentInfo.getFildetaljerListe().isEmpty());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon()),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoRep.get()));
		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(1L));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		skjermingService.setDokumentKassert(dokumentInfo, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKTIDLIGKASSASJON + dokumentInfo.getDokumentInfoId(),
				HttpMethod.DELETE,
				createNoAccessHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}


}
