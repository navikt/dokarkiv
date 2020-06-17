package no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

@Data
@AllArgsConstructor
public class OpprettJournalpostResult {

    private Journalpost journalpost;
    private boolean isAlreadyOpprettet;

}
