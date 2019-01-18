package no.nav.dokarkiv.core.repository;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentUrlInfoRepositoryBegrenset {

    private final DokumentUrlInfoRepository dokumentUrlInfoRepository;
    private final SkjermingService skjermingService;

    public DokumentUrlInfoRepositoryBegrenset(DokumentUrlInfoRepository dokumentUrlInfoRepository, SkjermingService skjermingService) {
        this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
        this.skjermingService = skjermingService;
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
		return Objects.nonNull(dokumentUrlInfo) && isFalse(skjermingService.isJournalpostBegrenset(dokumentUrlInfo.getJournalpost()
                .getJournalpostId(), SkjermingTypeCode.POL)) ? dokumentUrlInfo : null;
    }

    public Optional<DokumentUrlInfo> findByDoctoken(String doctoken) {
        Optional<DokumentUrlInfo> dokumentUrlInfo = dokumentUrlInfoRepository.findByDoctoken(doctoken);
        return dokumentUrlInfo.isPresent() && isFalse(skjermingService.isJournalpostBegrenset(dokumentUrlInfo.get()
                .getJournalpost()
                .getJournalpostId(), SkjermingTypeCode.POL)) ? dokumentUrlInfo : Optional.empty();
    }


}
