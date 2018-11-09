package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
public class JournalpostDokumentInfoRelasjonRepositoryBegrenset {

    private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

    public JournalpostDokumentInfoRelasjonRepositoryBegrenset(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
        this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
    }

    public Optional<List<JournalpostDokumentInfoRelasjon>> findByDokumentInfoId(Long dokumentInfoId) {
        List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoId)
                .orElse(new ArrayList<>());

        return Optional.of(journalpostDokumentInfoRelasjonList.stream()
                .filter(journalpostDokumentInfoRelasjon -> isFalse(journalpostDokumentInfoRelasjon.getJournalpost()
                        .isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT)))
                .collect(Collectors
                        .toList()));
    }

    public void delete(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
        journalpostDokumentInfoRelasjonRepository.delete(journalpostDokumentInfoRelasjon);
    }

    public JournalpostDokumentInfoRelasjon save(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
        return journalpostDokumentInfoRelasjonRepository.save(journalpostDokumentInfoRelasjon);
    }

}
