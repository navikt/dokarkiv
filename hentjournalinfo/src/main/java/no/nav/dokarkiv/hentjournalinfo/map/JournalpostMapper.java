package no.nav.dokarkiv.hentjournalinfo.map;

import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostType;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalpostMapper {

    public static Journalpost mapJournalpost(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
        return Journalpost.builder()
                .journalpostId(journalpost.getJournalpostId())
                .tema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
                .journalpostType(JournalpostType.mapFromJournalpostTypeCode(journalpost.getJournalposttype()))
                .journalpostStatus(JournalpostStatus.mapFromJournalStatusCode(journalpost.getJournalstatus()))
                .tittel(journalpost.getInnhold())
                .build();
    }
}
