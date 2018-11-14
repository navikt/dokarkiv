package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
public class DokumentinfoRepositoryBegrenset {


    private final DokumentinfoRepository dokumentinfoRepository;
    private final BegrensningService begrensningService;
    private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

    public DokumentinfoRepositoryBegrenset(DokumentinfoRepository dokumentinfoRepository, BegrensningService begrensningService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
        this.dokumentinfoRepository = dokumentinfoRepository;
        this.begrensningService = begrensningService;
        this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
    }

    public Optional<DokumentInfo> findDokumentInfoByJournalpostIdAndDokumentInfoId(Long journalpostId, Long dokumentinfoId) {
        Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(journalpostId, dokumentinfoId);
        return isFalse(dokumentInfo.isPresent()) ? Optional.empty() : begrensningService.isDokumentInfoBegrenset(dokumentinfoId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional
                .empty() : Optional.of(setBegrensetRelasjoner(dokumentInfo.get()));
    }

    public DokumentInfo save(DokumentInfo dokumentInfo) {
        return dokumentinfoRepository.save(dokumentInfo);
    }

    public void deleteAll() {
        dokumentinfoRepository.deleteAll();
    }

    public boolean existsById(Long id) {
        return dokumentinfoRepository.existsById(id) && isFalse(begrensningService.isDokumentInfoBegrenset(id, BegrensningTypeCode.UTILGJENGELIGGJORT));
    }

    public Optional<DokumentInfo> findById(Long id) {
        Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findById(id);
        return isFalse(dokumentInfo.isPresent()) ? Optional.empty() : begrensningService.isDokumentInfoBegrenset(id, BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional
                .empty() : Optional.of(setBegrensetRelasjoner(dokumentInfo.get()));
    }

    private DokumentInfo setBegrensetRelasjoner(DokumentInfo dokumentInfo) {
        List<Long> begrensetDokumentInfoIds = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonJournalpostIdByDokumentInfoId(dokumentInfo
                .getDokumentInfoId())
                .orElseGet(ArrayList::new)
                .stream()
                .map(BigInteger::longValue)
                .collect(Collectors.toList());
        dokumentInfo.addAllbegrensetRelasjonJournalpostIds(begrensetDokumentInfoIds);
        return dokumentInfo;
    }


}
