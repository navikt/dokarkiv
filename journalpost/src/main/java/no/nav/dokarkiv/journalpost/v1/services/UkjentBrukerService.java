package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;

@Component
public class UkjentBrukerService {

	private final JournalpostRepository journalpostRepository;
	private static final EnumSet<JournalStatusCode> validJournalStatuses = EnumSet.of(U, OD, M, MO);

	public UkjentBrukerService(final JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	public List<ArkivElementEndringTO> settUkjentBruker(long journalpostId) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(format("Kunne ikke finne journalpost med journalpostId=%d i joark", journalpostId)));

		JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
		if (validJournalStatuses.contains(oldJournalStatus)) {
			journalpost.setJournalstatus(UB);
		} else {
			throw new UgyldigJournalStatusException("Journalpost kan ikke settes til UB (ukjent bruker)");
		}

		return singletonList(ArkivElementEndringTO.builder()
				.arkivElement(JOURNALPOST_JOURNALSTATUS)
				.fraVerdi(oldJournalStatus.name())
				.tilVerdi(journalpost.getJournalstatus().name())
				.build());
	}
}
