package no.nav.dokarkiv.core.domain.service;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class BegrensningService {

    @Inject
    private BegrensningRepository begrensningRepository;

    public boolean isJournalpostBegrenset(Long journalpostId, BegrensningTypeCode begrensningTypeCode) {
        List<Begrensning> begrensning = begrensningRepository.findByJournalpostIdOnly(journalpostId, begrensningTypeCode.name())
                .orElse(new ArrayList<>());
        return isFalse(begrensning.isEmpty());
    }

    public boolean isDokumentInfoBegrenset(Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
        List<Begrensning> begrensning = begrensningRepository.findByDokumentInfoIdOnly(dokumentInfoId, begrensningTypeCode.name())
                .orElse(new ArrayList<>());
        return isFalse(begrensning.isEmpty());
    }

    public boolean isJournalpostDokumentInfoRelasjonBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
        List<Begrensning> begrensning = begrensningRepository.findByDokumentInfoIdJournalpostId(journalpostId, dokumentInfoId, begrensningTypeCode
                .name()).orElse(new ArrayList<>());
        return isFalse(begrensning.isEmpty());
    }


}
