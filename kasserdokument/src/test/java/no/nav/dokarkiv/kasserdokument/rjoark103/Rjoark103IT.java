package no.nav.dokarkiv.kasserdokument.rjoark103;

import static no.nav.dokarkiv.kasserdokument.util.TestUtil.KASSERT_AV_NAVN;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.createKasserDokumentRequest;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.dokarkiv.kasserdokument.AbstractKasserDokumentIT;
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
import java.util.stream.Collectors;

public class Rjoark103IT extends AbstractKasserDokumentIT {

	@Test
	public void skallIkkeKassereDokumentNårDokmentInfoIkkeFinnes() throws IOException {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoId), createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Kan ikke finne dokument med dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallKassereDokumentDokmumentKnyttetFlereJournalposter() throws IOException {
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

		ResponseEntity<KasserDokumentResponse> responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoRep.get()
						.getDokumentInfoId()), createHeadersWithAksjon()),
				KasserDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo1.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn(), is(KASSERT_AV_NAVN));
		assertNotNull(dokumentInfoAfter.get().getDatoKassert());
		assertTrue(dokumentInfoAfter.get().getFildetaljerListe().isEmpty());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoAfter.get()));

		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(2L));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.SLETT));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), nullValue());
		assertThat(aksjonsLogg.getDokumentInfoId(), is(dokumentInfo1.getDokumentInfoId()));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(3));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
						.arkivElement("FilDetaljer.variantFormat")
						.fraVerdi("ARKIV")
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("DokumentInfo.kassertAv")
						.fraVerdi(null)
						.tilVerdi(KASSERT_AV_NAVN)
						.build().toStringElementFraTil()

		));
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

		ResponseEntity<KasserDokumentResponse> responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoRep.get()
						.getDokumentInfoId()), createHeadersWithAksjon()),
				KasserDokumentResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn(), is(KASSERT_AV_NAVN));
		assertNotNull(dokumentInfoAfter.get().getDatoKassert());
		assertTrue(skjermingService.isDokumentInfoKassert(dokumentInfoAfter.get()));
		assertTrue(dokumentInfoAfter.get().getFildetaljerListe().isEmpty());
		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(1L));
	}

	@Test
	public void noAccess() {
		abacPermit();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(123L), createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
