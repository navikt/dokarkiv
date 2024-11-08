package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;

@Slf4j
@Component
public class SettAvbruttJournalpostTilRedigeringService {
	private final JournalpostRepository journalpostRepository;

	public SettAvbruttJournalpostTilRedigeringService(JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	@Transactional
	public void settAvbruttJournalpostTilRedigering(Long journalpostId) {
		Journalpost journalpost = journalpostRepository.fetchById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		JournalpostDokumentInfoRelasjon hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon();

		verifiserAvbruttJournalstatus(journalpost);
		verifiserHoveddokumentrelasjonFinnes(hoveddokument, journalpost);

		log.info("Journalpost med journalpostId={} er verifisert med avbrutt status. Endrer status til redigerbar", journalpost.getJournalpostId());
		journalpost.setJournalstatus(JournalStatusCode.D);
		hoveddokument.getDokumentInfo().setDokumentstatus(UNDER_REDIGERING);
	}

	private static void verifiserHoveddokumentrelasjonFinnes(JournalpostDokumentInfoRelasjon hoveddokument, Journalpost journalpost) {
		if (hoveddokument == null) {
			throw new JournalpostIkkeFunnetException(String.format("Journalpost med journalpostId=%s mangler hoveddokumentrelasjon og kan derfor ikke tilbakestilles til redigerbar tilstand.", journalpost.getJournalpostId()));
		}
	}

	private static void verifiserAvbruttJournalstatus(Journalpost journalpost) {
		if (JournalStatusCode.A != journalpost.getJournalstatus()) {
			throw new UgyldigJournalStatusException(String.format("Journalpost med journalpostId=%s kan ikke gjennopprettes fra avbrutt da den ikke har status=%s. Journalposten har status=%s", journalpost.getJournalpostId(), JournalStatusCode.A, journalpost.getJournalstatus()));
		}
	}
}
