package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void skalAngreLogiskSlettDokument_avVedlegg_medVedleggUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		List<Begrensning> begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(1L, begrensetJp.size());
		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(0L, begrensetJp.size());
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_avVedlegg_ettersomVedleggIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));

		List<Begrensning> begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(begrensetJp.size(), 0L);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunVedleggBegrensningen_nårVedleggOgHoveddokumentErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		List<Begrensning> begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertEquals(begrensetJp.size(), 1L);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(begrensetJp.size(), 1L);
		assertThat(hentAntallBegrensninger(), is(2L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertEquals(begrensetJp.size(), 1L);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(begrensetJp.size(), 0L);
		assertThat(hentAntallBegrensninger(), is(1L));
	}


	@Test
	public void skalAngreLogiskSlettDokument_avHoveddokument_medHoveddokumentUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		List<Begrensning> begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);

		assertEquals(begrensetJp.size(), 1L);
		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertEquals(begrensetJp.size(), 0L);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));

		List<Begrensning> begrensetJp = hentJournalpostEtterUtfoertKall(journalpost.getJournalpostId());
		assertEquals(begrensetJp.size(), 0L);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunHoveddokumentBegrensningen_nårVedleggOgHoveddokumentErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		List<Begrensning> begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertEquals(begrensetJp.size(), 1L);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(begrensetJp.size(), 1L);
		assertThat(hentAntallBegrensninger(), is(2L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" +
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertEquals(begrensetJp.size(), 0L);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertEquals(begrensetJp.size(), 1L);
		assertThat(hentAntallBegrensninger(), is(1L));
	}

}
