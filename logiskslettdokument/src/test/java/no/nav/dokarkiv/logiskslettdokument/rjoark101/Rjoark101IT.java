package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void skalIkkeAngreLogiskSlettDokument_ettersomJournalpostDokumentInfoRelasjonIkkeFinnes() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + ikkeEksisterendeDokumentInfoId,
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost.getJournalpostId(),
						ikkeEksisterendeDokumentInfoId)));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avVedlegg_medVedleggUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		begrensningService.setJpDokInfoRelBegrensning(rel, BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		JournalpostDokumentInfoRelasjon relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);

		assertThat(relEtterKall.getBegrensning(), is(BegrensningTypeCode.POL));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertNull(relEtterKall.getBegrensning());
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_avVedlegg_ettersomVedleggIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId(),
						BegrensningTypeCode.POL)));

		JournalpostDokumentInfoRelasjon relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).orElse(null);
		assertNull(relEtterKall.getBegrensning());
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunVedleggBegrensningen_naarVedleggOgHoveddokumentErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		begrensningService.setJpDokInfoRelBegrensning(rel, BegrensningTypeCode.POL);
		begrensningService.setJournalpostBegrensning(rel.getJournalpost(), BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		JournalpostDokumentInfoRelasjon relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertThat(relEtterKall.getBegrensning(), is(BegrensningTypeCode.POL));


		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + relEtterKall.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);

		assertNotNull(begrensetJp.getBegrensning());

		relEtterKall = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);

		assertNull(relEtterKall.getBegrensning());
	}

	@Test
	public void skalAngreLogiskSlettDokument_avHoveddokument_medHoveddokumentUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		begrensningService.setJournalpostBegrensning(journalpost, BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNotNull(begrensetJp.getBegrensning());

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNull(begrensetJp.getBegrensning());
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						BegrensningTypeCode.POL)));

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNull(begrensetJp.getBegrensning());
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunHoveddokumentBegrensningen_naarVedleggOgHoveddokumentErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		begrensningService.setJournalpostBegrensning(journalpost, BegrensningTypeCode.POL);
		begrensningService.setJpDokInfoRelBegrensning(rel, BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Journalpost begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNotNull(begrensetJp.getBegrensning());
		JournalpostDokumentInfoRelasjon begrensetRel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertNotNull(begrensetRel.getBegrensning());

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" +
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = joarkRepository.findById(journalpost.getJournalpostId()).orElse(null);
		assertNull(begrensetJp.getBegrensning());
		begrensetRel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).orElse(null);
		assertNotNull(begrensetRel.getBegrensning());
	}

	@Test
	public void skalIkkeAngreLogiskSletteDokument_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		JournalpostDokumentInfoRelasjon sammensattDok = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next();

		begrensningService.setJpDokInfoRelBegrensning(sammensattDok, BegrensningTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + sammensattDok.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
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
		begrensningService.setJournalpostBegrensning(journalpost, BegrensningTypeCode.POL);

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
