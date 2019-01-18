package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.utilgjengeliggjoerHoveddokument;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.utilgjengeliggjoerVedlegg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark100IT extends AbstractSlettDokumentIT {

	@Test
	public void skalLogiskSletteDokument_avVedlegg() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensninger);

		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_avVedlegg_ettersomVedleggErPOL() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er POL.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId())));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_avVedlegg_ettersomHoveddokumentErPOL() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er POL",
						journalpost.getJournalpostId())));
	}

	@Test
	public void skalLogiskSletteDokument_avHoveddokument() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensninger);

		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_avHoveddokument_ettersomHoveddokumentErPOL() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er POL",
						journalpost.getJournalpostId())));
	}

	@Test
	public void skalLogiskSletteDokument_avHoveddokument_evenNaarVedleggErPOL() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning hoveddokumentBegrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(hoveddokumentBegrensninger);

		Begrensning vedleggBegrensninger = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(vedleggBegrensninger);

		assertThat(hentAntallBegrensninger(), is(2L));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomJournalpostIdIkkeFinnes() {
		abacPermit();

		Long ikkeEksisterendeJournalpostId = 13L;
		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + ikkeEksisterendeJournalpostId + "/" + ikkeEksisterendeDokumentInfoId,
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Journalpost ikke funnet. journalpostId=%s",
						ikkeEksisterendeJournalpostId)));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomJournalpostDokumentInfoRelasjonIkkeFinnes() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + ikkeEksisterendeDokumentInfoId,
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
	public void skalIkkeLogiskSletteDokument_ettersomIngenRelasjonMellomInputJournalpostIdOgInputDokumentInfoIdFinnes() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + journalpost2
						.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost1.getJournalpostId(),
						journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		DokumentInfo sammensattDok = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + sammensattDok.getDokumentInfoId(),
				HttpMethod.POST,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke logisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						journalpost.getJournalpostId(),
						sammensattDok.getDokumentInfoId())));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		Begrensning jpBegrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.begrensningType(SkjermingTypeCode.POL)
				.build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		begrensningRepository.save(jpBegrensning);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccesHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

}
