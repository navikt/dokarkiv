package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class LeggTilLogiskVedleggService {
    private final SkannetInnholdRepository skannetInnholdRepository;
    private final DokumentinfoRepository dokumentinfoRepository;

    @Inject
    public LeggTilLogiskVedleggService(final SkannetInnholdRepository skannetInnholdRepository, final DokumentinfoRepository dokumentinfoRepository) {
        this.skannetInnholdRepository = skannetInnholdRepository;
        this.dokumentinfoRepository = dokumentinfoRepository;
    }
    public String leggTilLogiskVedlegg(String dokumentInfoId, LeggTilLogiskVedleggRequest request) {
        DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(Long.parseLong(dokumentInfoId))
                .orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Kunne ikke finne dokumentInfo med dokumentInfoId=%s i joark", dokumentInfoId)));

        SkannetInnhold skannetInnhold = SkannetInnhold.builder().vedleggInnhold(request.getTittel()).build();
        skannetInnhold.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

        dokumentInfo.addSkannetInnhold(skannetInnhold);
        skannetInnhold = skannetInnholdRepository.save(skannetInnhold);

        return skannetInnhold.getSkannetInnholdId().toString();
    }
}
