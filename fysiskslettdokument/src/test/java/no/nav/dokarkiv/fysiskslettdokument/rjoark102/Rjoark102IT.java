package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.BEGRENSNINGTYPE_UTILGJENGELIGGJORT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettDuplikatRelasjon;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.utilgjengeliggjoerHoveddokument;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.utilgjengeliggjoerVedlegg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.fysiskslettdokument.AbstractFysiskSlettDokumentIT;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomJournalpostDokumentInfoRelasjonMangler() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		Long feilDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ feilDokumentInfoId + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost.getJournalpostId(),
						feilDokumentInfoId)));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomIngenRelasjonMellomInputJournalpostIdOgInputDokumentInfoIdFinnes() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() + "/"
						+ BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost1.getJournalpostId(),
						journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomJournalpostDokumentInfoRelasjonIkkeErUnik() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		opprettDuplikatRelasjon(journalpost.findHoveddokumentDokumentInfoRelasjon());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
		assertThat(responseEntity.getBody(), containsString(
				String.format("query did not return a unique result")));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		DokumentInfo sammensattDok = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), sammensattDok.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ sammensattDok.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke fysisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						journalpost.getJournalpostId(),
						sammensattDok.getDokumentInfoId())));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_avVedlegg_ettersomVedleggIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));
	}

	@Test
	public void skalSletteDokumentFysisk_avVedlegg_somErKnyttetEnJournalpost() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertFalse(vedlegg.isRelatedToMultipleJournalposts());
		assertThat(hentAntallBegrensninger(), is(1L));

		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				vedlegg.getDokumentInfoId()).size() > 0);


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensninger);
		assertThat(hentAntallBegrensninger(), is(0L));

		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost.getJournalpostId()).isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				vedlegg.getDokumentInfoId()).size() > 0);
	}

	@Test
	public void skalSletteDokumentFysisk_avVedlegg_somErKnyttetToJournalposter_skalKunSletteRelasjon() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertTrue(vedlegg.isRelatedToMultipleJournalposts());
		assertThat(hentAntallBegrensninger(), is(1L));

		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId()).get().size());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());


		assertEquals(2L, joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				vedlegg.getDokumentInfoId()).size());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + vedlegg.getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentVedleggBegrensningEtterUtfoertKall(journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensninger);
		assertThat(hentAntallBegrensninger(), is(0L));

		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost1.getJournalpostId()).isPresent());
		assertEquals(1L, journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId()).get().size());

		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertEquals(1L, joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				vedlegg.getDokumentInfoId()).size());
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId() +
						"/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokument_utenAndreRelasjoner() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(hentAntallBegrensninger(), is(1L));

		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				hoveddokument.getDokumentInfoId()).size() > 0);

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost.getJournalpostId());
		assertNull(begrensninger);
		assertThat(hentAntallBegrensninger(), is(0L));

		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost.getJournalpostId()).isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				hoveddokument.getDokumentInfoId()).size() > 0);
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokumentOgEtKnyttetVedleggDerHoveddokumentErBegrenset_skalSletteAlt() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(hentAntallBegrensninger(), is(1L));

		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).get().size());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId())
				.isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
				.isPresent());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost.getJournalpostId());
		assertNull(begrensninger);
		assertThat(hentAntallBegrensninger(), is(0L));

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId())
				.isPresent());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
				.isPresent());

		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_medHoveddokumentOgEtKnyttetVedleggDerVedleggErBegrenset_skalKasteBegrensningIkkeFunnetException() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokumentOgEtKnyttetVedleggDerHoveddokumentErBegrensetMenVedleggErKnyttetAnnenJournalpost_skalIkkeSletteAndreJournalpostRelasjonen() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo hoveddokument2 = journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost1.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertTrue(vedlegg.isRelatedToMultipleJournalposts());
		assertThat(hentAntallBegrensninger(), is(1L));

		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).get().size());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).get().size());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isPresent());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
				.get()
				.size());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ hoveddokument1.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost1.getJournalpostId());
		assertNull(begrensninger);
		assertThat(hentAntallBegrensninger(), is(0L));

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).isPresent());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).get().size());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isPresent());
		assertEquals(1L, journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
				.get()
				.size());

		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokumentDerHoveddokumentErBegrensetMenKnyttetTilAnnenJournalpostSomVedlegg_skalIkkeSletteAndreJournalpostRelasjonen() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo hoveddokument2 = journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);
		JournalpostDokumentInfoRelasjon jp1RelasjonSomVedleggTilJp2 = hoveddokument1.findJournalpostRelasjonByJournalpostId(journalpost2
				.getJournalpostId());
		journalpostDokumentInfoRelasjonRepository.save(jp1RelasjonSomVedleggTilJp2);

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost1.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());
		assertThat(hentAntallBegrensninger(), is(1L));

		assertEquals(1L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).get().size());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).get().size());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isPresent());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ hoveddokument1.getDokumentInfoId() + "/" + BEGRENSNINGTYPE_UTILGJENGELIGGJORT,
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Begrensning begrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost1.getJournalpostId());
		assertNull(begrensninger);
		assertThat(hentAntallBegrensninger(), is(0L));

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).isPresent());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).get().size());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isPresent());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isPresent());

		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());
	}

}
