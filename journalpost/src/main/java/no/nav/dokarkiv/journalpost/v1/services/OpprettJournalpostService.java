package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

/// Orkestrerer opprettelse av journalpost slik at eksterne web-kall (PDL/Ereg) og PDF/A-validering
/// skjer utenfor databasetransaksjonen. Denne klassen skal ikke være `@Transactional`.
@Service(value = "opprettNyJournalpostService")
public class OpprettJournalpostService {

	public static final String UKJENT = "UKJENT";

	private final OpprettJournalpostOppslagService opprettJournalpostOppslagService;
	private final OpprettJournalpostTransaksjonService opprettJournalpostTransaksjonService;

	public OpprettJournalpostService(final OpprettJournalpostOppslagService opprettJournalpostOppslagService,
									 final OpprettJournalpostTransaksjonService opprettJournalpostTransaksjonService) {
		this.opprettJournalpostOppslagService = opprettJournalpostOppslagService;
		this.opprettJournalpostTransaksjonService = opprettJournalpostTransaksjonService;
	}

	public OpprettJournalpostResult opprettJournalpost(OpprettJournalpostRequest request) {
		Optional<OpprettJournalpostResult> eksisterende = opprettJournalpostTransaksjonService.finnEksisterendeJournalpost(request.getEksternReferanseId());
		if (eksisterende.isPresent()) {
			return eksisterende.get();
		}

		OpprettJournalpostOppslag oppslag = opprettJournalpostOppslagService.hentOppslag(request);
		 return opprettJournalpostTransaksjonService.opprettJournalpost(request, oppslag);
	}
}
