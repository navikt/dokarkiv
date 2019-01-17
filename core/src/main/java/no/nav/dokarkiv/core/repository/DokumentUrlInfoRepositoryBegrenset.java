package no.nav.dokarkiv.core.repository;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.service.BegrensningService;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentUrlInfoRepositoryBegrenset {

    private final DokumentUrlInfoRepository dokumentUrlInfoRepository;
    private final BegrensningService begrensningService;

    public DokumentUrlInfoRepositoryBegrenset(DokumentUrlInfoRepository dokumentUrlInfoRepository, BegrensningService begrensningService) {
        this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
        this.begrensningService = begrensningService;
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
		return Objects.nonNull(dokumentUrlInfo) && isFalse(begrensningService.isJournalpostBegrenset(dokumentUrlInfo.getJournalpost()
                .getJournalpostId(), BegrensningTypeCode.POL)) ? dokumentUrlInfo : null;
    }

    public Optional<DokumentUrlInfo> findByDoctoken(String doctoken) {
        Optional<DokumentUrlInfo> dokumentUrlInfo = dokumentUrlInfoRepository.findByDoctoken(doctoken);
        return dokumentUrlInfo.isPresent() && isFalse(begrensningService.isJournalpostBegrenset(dokumentUrlInfo.get()
                .getJournalpost()
                .getJournalpostId(), BegrensningTypeCode.POL)) ? dokumentUrlInfo : Optional.empty();
    }
}
