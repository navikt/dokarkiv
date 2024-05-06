package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.lang.Long.parseLong;
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
	private static final List<JournalStatusCode> validJournalStatusList = Arrays.asList(U, OD, M, MO);

	public UkjentBrukerService(final JournalpostRepository journalpostRepository) {
		this.journalpostRepository = journalpostRepository;
	}

	public List<ArkivElementEndringTO> settUkjentBruker(String journalpostId) {
		Journalpost journalpost = journalpostRepository.findById(parseLong(journalpostId))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
		if (validJournalStatusList.contains(oldJournalStatus)) {
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
