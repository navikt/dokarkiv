package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createDokumentInfo;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.createJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.Set;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void shouldAngreLogiskSlettVedlegg() {
		abacPermit();
		MDC.put(MDCConstants.MDC_USER_NAME, OPPRETTET_KILDE_NAVN);

		Journalpost journalpost = createJournalpostBuilder().build();
		DokumentInfo vedlegg = createDokumentInfo();
		JournalpostDokumentInfoRelasjon vedleggRel = JournalpostDokumentInfoRelasjon.builder().journalpost(journalpost).dokumentInfo(vedlegg).tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).build();
		vedleggRel.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		vedleggRel.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		journalpost.addJournalpostDokumentInfoRelasjon(vedleggRel);
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Set<JournalpostDokumentInfoRelasjon> dokumentInfos = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		Long vedleggId = dokumentInfos.iterator().next().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedleggId,
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		List<Begrensning> begrensetJp = hentVedleggBegrensningEtterUtførtKall(vedleggId);
		assertEquals(1L, begrensetJp.size());

		responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedleggId,
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentVedleggBegrensningEtterUtførtKall(vedleggId);
		assertEquals(0L, begrensetJp.size());
	}

	@Test
	public void shouldAngreLogiskSlettHoveddokument() {
		abacPermit();
		MDC.put(MDCConstants.MDC_USER_NAME, OPPRETTET_KILDE_NAVN);

		Journalpost journalpost = createJournalpostBuilder().build();
		joarkRepository.save(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		List<Begrensning> begrensetJp = hentHoveddokumentBegrensningEtterUtførtKall(journalpost);

		assertEquals(begrensetJp.size(), 1L);

		responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.PATCH,
				createHeaders(),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtførtKall(journalpost);

		assertEquals(begrensetJp.size(), 0L);
	}

	@Test
	public void shouldFailToAngreLogiskSlettDokumentBecauseDocumentWasNotDeleted() {
		abacPermit();
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark101");

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
				String.format("%s kan ikke angre logisk sletting av journalpost med journalpostId=%s. Journalposten er ikke logisk slettet",
						MDC.get(MDCConstants.MDC_REQUEST_ID),
						journalpost.getJournalpostId())));

		List<Begrensning> begrensetJp = hentJournalpostEtterUtførtKall (journalpost.getJournalpostId());
		assertEquals(begrensetJp.size(), 0L);
	}
}
