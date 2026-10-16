package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/// Orkestrerer opprettelse av journalpost slik at eksterne web-kall (PDL/Ereg/dokarkivmellomlager) og PDF/A-validering
/// skjer utenfor databasetransaksjonen. Denne klassen skal ikke være `@Transactional`.
@Service(value = "opprettNyJournalpostService")
public class OpprettJournalpostService {

	private final OpprettJournalpostOppslagService opprettJournalpostOppslagService;
	private final OpprettJournalpostTransaksjonService opprettJournalpostTransaksjonService;
	private final OpprettJournalpostDokumentFilService opprettJournalpostDokumentFilService;

	public OpprettJournalpostService(OpprettJournalpostOppslagService opprettJournalpostOppslagService,
									 OpprettJournalpostTransaksjonService opprettJournalpostTransaksjonService,
									 OpprettJournalpostDokumentFilService opprettJournalpostDokumentFilService) {
		this.opprettJournalpostOppslagService = opprettJournalpostOppslagService;
		this.opprettJournalpostTransaksjonService = opprettJournalpostTransaksjonService;
		this.opprettJournalpostDokumentFilService = opprettJournalpostDokumentFilService;
	}

	public OpprettJournalpostResult opprettJournalpost(OpprettJournalpostRequest request) {
		Optional<OpprettJournalpostResult> eksisterende = opprettJournalpostTransaksjonService.finnEksisterendeJournalpost(request.getEksternReferanseId());
		if (eksisterende.isPresent()) {
			return eksisterende.get();
		}

		Set<UUID> dokumentFilIds = opprettJournalpostDokumentFilService.validerDokumentFiler(request);
		opprettJournalpostDokumentFilService.holdDokumentFiler(dokumentFilIds);
		OpprettJournalpostOppslag oppslag = opprettJournalpostOppslagService.hentOppslag(request);

		return opprettJournalpostTransaksjonService.opprettJournalpost(request, oppslag);
	}
}
