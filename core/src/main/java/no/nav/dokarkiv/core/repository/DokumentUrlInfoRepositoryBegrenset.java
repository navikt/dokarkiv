package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Transactional
public class DokumentUrlInfoRepositoryBegrenset {

    private final DokumentUrlInfoRepository dokumentUrlInfoRepository;

    @Lazy
    public DokumentUrlInfoRepositoryBegrenset(DokumentUrlInfoRepository dokumentUrlInfoRepository) {
        this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
    }

    public DokumentUrlInfo save(DokumentUrlInfo dokumentUrlInfo) {
        return dokumentUrlInfoRepository.save(dokumentUrlInfo);
    }

    /**
     * Skal bare brukes i test!!
     */
    public void deleteAll() {
        dokumentUrlInfoRepository.deleteAll();
    }

    public DokumentUrlInfo findByFilUuid(String filUuid) {
        DokumentUrlInfo dokumentUrlInfo = dokumentUrlInfoRepository.findByFilUuid(filUuid);
        return dokumentUrlInfo.getJournalpost().isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : dokumentUrlInfo;
    }

    public Optional<DokumentUrlInfo> findByDoctoken(String doctoken) {
        Optional<DokumentUrlInfo> dokumentUrlInfo = dokumentUrlInfoRepository.findByDoctoken(doctoken);
        return dokumentUrlInfo.filter(dokUrlInfo -> isFalse(dokUrlInfo.getJournalpost()
                .isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))).isPresent() ? dokumentUrlInfo : Optional.empty();
    }


}
