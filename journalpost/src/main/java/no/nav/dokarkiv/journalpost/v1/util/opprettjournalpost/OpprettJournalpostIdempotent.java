package no.nav.dokarkiv.journalpost.v1.util.opprettjournalpost;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class OpprettJournalpostIdempotent {

    private final JoarkRepository joarkRepository;

    @Inject
    public OpprettJournalpostIdempotent(final JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public boolean isJournalpostWithKanalSkanImAndEksternReferanseIdAlreadyInDb(OpprettJournalpostRequest request) {
        if (!MottaksKanalCode.SKAN_IM.name().equals(request.getKanal())) {
            return false;
        }
        return joarkRepository.findJournalpostIdWithKanalSkanImByKanalReferanseId(request.getEksternReferanseId()) != null;
    }
}
