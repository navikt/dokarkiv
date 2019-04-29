package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.exceptions.LogiskVedleggIkkeFunnetException;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class SlettLogiskVedleggService {
    private final SkannetInnholdRepository skannetInnholdRepository;

    @Inject
    public SlettLogiskVedleggService(final SkannetInnholdRepository skannetInnholdRepository) {
        this.skannetInnholdRepository = skannetInnholdRepository;
    }

    public void slettLogiskVedlegg(String dokumentInfoId, String logiskVedleggId) {
        skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggId, dokumentInfoId)
                .orElseThrow(() -> new LogiskVedleggIkkeFunnetException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggId, dokumentInfoId)));

        skannetInnholdRepository.deleteSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggId, dokumentInfoId);
    }
}
