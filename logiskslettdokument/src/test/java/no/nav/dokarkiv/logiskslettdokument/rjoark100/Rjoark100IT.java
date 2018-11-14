package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.util.TestUtils;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;

public class Rjoark100IT extends AbstractSlettDokumentIT {

	@Test
	public void skalLogiskSletteDokumentKnyttetKunEnJournalpost_avVedlegg() {
		abacPermit();

		Journalpost journalpost = TestUtils.createJournalpostBuilder().build();
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
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<Begrensning> begrensninger = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertThat(begrensninger.size(), is(1));


		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void skalIkkeLogiskSletteDokumentKnyttetKunEnJournalpost_avVedlegg_ettersomVedleggErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = TestUtils.createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		Begrensning jpBegrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(vedlegg.getDokumentInfoId())
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		begrensningRepository.save(jpBegrensning);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er utilgjengeliggjort.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId())));
	}

	@Test
	public void skalIkkeLogiskSletteDokumentKnyttetKunEnJournalpost_avVedlegg_ettersomHoveddokumentErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = TestUtils.createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		Begrensning jpBegrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		begrensningRepository.save(jpBegrensning);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er utilgjengeliggjort",
						journalpost.getJournalpostId())));
	}

	@Test
	public void skalLogiskSletteDokumentKnyttetKunEnJournalpost_avHoveddokument() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(TestUtils.createJournalpostBuilder().journalpostId(JOURNALPOST_ID).build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<Begrensning> begrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertThat(begrensninger.size(), is(1));

		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void skalIkkeLogiskSletteDokumentKnyttetKunEnJournalpost_avHoveddokument_ettersomHoveddokumentErUtilgjengeliggjort() {
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
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er utilgjengeliggjort",
						journalpost.getJournalpostId())));
	}

	@Test
	public void skalLogiskSletteDokumentKnyttetKunEnJournalpost_avHoveddokument_evenNaarVedleggErUtilgjengeliggjort() {
		abacPermit();

		Journalpost journalpost = TestUtils.createJournalpostBuilder().build();
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder()
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(createDokumentInfo())
				.build());
		joarkRepository.save(journalpost);

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		Begrensning jpBegrensning = Begrensning.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(vedlegg.getDokumentInfoId())
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		jpBegrensning.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		begrensningRepository.save(jpBegrensning);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<Begrensning> hoveddokumentBegrensninger = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertThat(hoveddokumentBegrensninger.size(), is(1));

		List<Begrensning> vedleggBegrensninger = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertThat(vedleggBegrensninger.size(), is(1));

		assertThat(hentAntallBegrensninger(), is(2L));
	}

	@Test
	public void skalIkkeLogiskSletteDokumentKnyttetKunEnJournalpost_ettersomJournalpostDokumentInfoRelasjonMangler() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().journalpostId(JOURNALPOST_ID).build());

		Long feilDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + feilDokumentInfoId,
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
				feilDokumentInfoId)));
	}

	@Test
	public void skalIkkeLogiskSletteDokumentKnyttetKunEnJournalpost_ettersomIngenRelasjonMellomInputJournalpostIdOgInputDokumentInfoIdFinnes() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());
		Journalpost journalpost2 = joarkRepository.save(TestUtils.createJournalpostBuilder().build());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + journalpost2
						.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost1.getJournalpostId(),
						journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeLogiskSletteDokumentKnyttetKunEnJournalpost_ettersomDokumentInfoErKnyttetTilToJournalposter() {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(TestUtils.createJournalpostBuilder().journalpostId(JOURNALPOST_ID).build());
		Journalpost journalpost2 = TestUtils.createJournalpostBuilder().journalpostId(2L)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo())
								.build())
				.build();
		joarkRepository.save(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + journalpost1.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke slette dokument med dokumentInfoId=%s fordi dokumentet er knyttet til flere journalposter.",
						journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}
}
