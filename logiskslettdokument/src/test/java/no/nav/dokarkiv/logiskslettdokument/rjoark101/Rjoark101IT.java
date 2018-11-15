package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import no.nav.dokarkiv.logiskslettdokument.util.TestUtils;
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

		Begrensning begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensetJp);
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

		Begrensning begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensetJp);
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

		Begrensning begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(2L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensetJp);
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

		Begrensning begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);

		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNull(begrensetJp);
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

		Begrensning begrensetJp = hentJournalpostEtterUtfoertKall(journalpost.getJournalpostId());
		assertNull(begrensetJp);
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

		Begrensning begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(2L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" +
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(TestUtils.createJournalpostBuilder()
				.build());
		Begrensning jpBegrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		begrensningRepository.save(jpBegrensning);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createNoAccesHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

}
