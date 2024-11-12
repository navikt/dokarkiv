package no.nav.dokarkiv.journalpost.v1.services.dokvaktmester;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalStatusIkkeAvbruttException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;

@Slf4j
@Component
public class SettAvbruttJournalpostRedigerbarService {
	private final JournalpostRepository journalpostRepository;

	public SettAvbruttJournalpostRedigerbarService(JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	@Transactional
	public void settAvbruttJournalpostRedigerbar(Long journalpostId) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Kunne ikke finne journalpost med journalpostId=%s i joark".formatted(journalpostId)));

		verifiserJournalstatusAvbrutt(journalpost);
		verifiserHoveddokumentrelasjonFinnes(journalpost);

		journalpost.setJournalstatus(D);

		JournalpostDokumentInfoRelasjon hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokument.getDokumentInfo().setDokumentstatus(UNDER_REDIGERING);
	}

	private static void verifiserHoveddokumentrelasjonFinnes(Journalpost journalpost) {
		if (!journalpost.hasHoveddokumentRelasjon()) {
			throw new JournalpostIkkeFunnetException("Journalpost med journalpostId=%s mangler hoveddokumentrelasjon og kan ikke settes redigerbar.".formatted(
					journalpost.getJournalpostId()
			));
		}
	}

	private static void verifiserJournalstatusAvbrutt(Journalpost journalpost) {
		if (JournalStatusCode.A != journalpost.getJournalstatus()) {
			throw new JournalStatusIkkeAvbruttException("Journalpost med journalpostId=%s og status=%s kan ikke settes redigerbar. Journalposten må ha status=A.".formatted(
					journalpost.getJournalpostId(),
					journalpost.getJournalstatus()
			));
		}
	}
}
