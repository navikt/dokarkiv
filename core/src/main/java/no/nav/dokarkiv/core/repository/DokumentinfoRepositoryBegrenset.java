package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
public class DokumentinfoRepositoryBegrenset {


    private final DokumentinfoRepository dokumentinfoRepository;

    public DokumentinfoRepositoryBegrenset(DokumentinfoRepository dokumentinfoRepository) {
        this.dokumentinfoRepository = dokumentinfoRepository;
    }

    public Optional<DokumentInfo> findDokumentInfoByJournalpostIdAndDokumentInfoId(String originalJournalpostId, String dokumentinfoId) {
        Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findDokumentInfoByJournalpostIdAndDokumentInfoId(originalJournalpostId, dokumentinfoId);

        return dokumentInfo.filter(dokInfo -> isFalse(dokInfo.isBegrenset(Long.valueOf(originalJournalpostId), BegrensningTypeCode.UTILGJENGELIGGJORT)))
                .isPresent() ? dokumentInfo : Optional.empty();
    }

    public DokumentInfo save(DokumentInfo dokumentInfo) {
        return dokumentinfoRepository.save(dokumentInfo);
    }

    public void deleteAll() {
        dokumentinfoRepository.deleteAll();
    }

    public boolean existsById(Long id) {
        return dokumentinfoRepository.findById(id)
                .filter(dokInfo -> isFalse(dokInfo.isBegrenset(null, BegrensningTypeCode.UTILGJENGELIGGJORT)))
                .isPresent();
    }

    public Optional<DokumentInfo> findById(Long id) {
        return dokumentinfoRepository.findById(id)
                .filter(dokumentInfo -> isFalse(dokumentInfo.isBegrenset(null, BegrensningTypeCode.UTILGJENGELIGGJORT)));
    }

}
