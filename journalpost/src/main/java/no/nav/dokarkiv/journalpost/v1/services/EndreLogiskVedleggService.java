package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.LogiskVedleggIkkeFunnetException;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class EndreLogiskVedleggService {
    private final SkannetInnholdRepository skannetInnholdRepository;

    @Inject
    public EndreLogiskVedleggService(final SkannetInnholdRepository skannetInnholdRepository) {
        this.skannetInnholdRepository = skannetInnholdRepository;
    }

    public void endreLogiskVedlegg(String dokumentInfoId, String logiskVedleggId, EndreLogiskVedleggRequest request) {
        SkannetInnhold skannetInnhold = skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggId, dokumentInfoId)
                .orElseThrow(() -> new LogiskVedleggIkkeFunnetException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggId, dokumentInfoId)));

        skannetInnhold.setVedleggInnhold(request.getTittel());
        skannetInnhold.setEndretKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
        skannetInnholdRepository.save(skannetInnhold);
    }
}
