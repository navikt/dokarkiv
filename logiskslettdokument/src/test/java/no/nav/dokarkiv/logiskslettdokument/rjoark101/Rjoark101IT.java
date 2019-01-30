package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
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

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Optional<JournalpostDokumentInfoRelasjon> relRep = journalpostDokumentInfoRelasjonRepository.findById(rel.getId());
		assertTrue(relRep.isPresent());
		assertThat(relRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Optional<JournalpostDokumentInfoRelasjon> relRep = journalpostDokumentInfoRelasjonRepository.findById(rel.getId());
		assertTrue(relRep.isPresent());
		assertThat(relRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeaders()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_ettersomJournalpostDokumentInfoRelasjonIkkeFinnes() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + ikkeEksisterendeDokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost.getJournalpostId(),
						ikkeEksisterendeDokumentInfoId)));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avVedlegg_medVedleggUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		JournalpostDokumentInfoRelasjon relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);

		assertThat(relEtterKall.getSkjermingType(), is(SkjermingTypeCode.POL));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertNull(relEtterKall.getSkjermingType());
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_avVedlegg_ettersomVedleggIkkeErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet skjerming for dokument med journalpostId=%s, dokumentInfoId=%s og skjermingType=%s.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId(),
						SkjermingTypeCode.POL)));

		JournalpostDokumentInfoRelasjon relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).orElse(null);
		assertNull(relEtterKall.getSkjermingType());
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunVedleggBegrensningen_naarVedleggOgHoveddokumentErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);
		skjermingService.setJournalpostBegrensning(rel.getJournalpost(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		JournalpostDokumentInfoRelasjon relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertThat(relEtterKall.getSkjermingType(), is(SkjermingTypeCode.POL));


		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + relEtterKall.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);

		assertNotNull(begrensetJp.getSkjermingType());

		relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);

		assertNull(relEtterKall.getSkjermingType());
	}

	@Test
	public void skalAngreLogiskSlettDokument_avHoveddokument_medHoveddokumentUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNotNull(begrensetJp.getSkjermingType());

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNull(begrensetJp.getSkjermingType());
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet skjerming for journalpost med journalpostId=%s og skjermingType=%s.",
						journalpost.getJournalpostId(),
						SkjermingTypeCode.POL)));

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNull(begrensetJp.getSkjermingType());
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunHoveddokumentBegrensningen_naarVedleggOgHoveddokumentErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);
		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNotNull(begrensetJp.getSkjermingType());
		JournalpostDokumentInfoRelasjon begrensetRel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertNotNull(begrensetRel.getSkjermingType());

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" +
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNull(begrensetJp.getSkjermingType());
		begrensetRel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertNotNull(begrensetRel.getSkjermingType());
	}

	@Test
	public void skalIkkeAngreLogiskSletteDokument_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		JournalpostDokumentInfoRelasjon sammensattDok = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(sammensattDok, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + sammensattDok.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke angre logisk sletting av dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						journalpost.getJournalpostId(),
						sammensattDok.getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccesHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

}
