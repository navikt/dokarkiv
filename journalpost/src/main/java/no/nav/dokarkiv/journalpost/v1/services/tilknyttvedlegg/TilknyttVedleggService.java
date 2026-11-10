package no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.services.AccessLookupJournalpost;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;

@Slf4j
@Service(value = "tilknyttVedleggService")
public class TilknyttVedleggService {

    private final TilknyttVedleggTransaksjonService tilknyttVedleggTransaksjonService;
    private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator;
    private final AccessLookupJournalpost accessLookupJournalpost;
    public static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";

    public TilknyttVedleggService(TilknyttVedleggTransaksjonService tilknyttVedleggTransaksjonService,
                                  AccessLookupJournalpost accessLookupJournalpost) {
        this.tilknyttVedleggTransaksjonService = tilknyttVedleggTransaksjonService;
        this.accessLookupJournalpost = accessLookupJournalpost;
        this.tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();
    }

    public List<FeiledeDokumenter> tilknyttVedlegg(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {

        String tilknyttetAvNavn = MDC.get(MDC_USER_NAME);
        var request = new TilknyttVedleggRequest(tilknyttetAvNavn, tilknyttVedleggRequest.getDokument());
        tilknyttVedleggRequestValidator.validateRequest(request);
        var accessControlledDocuments = accessLookupJournalpost.checkDocumentsCanBeAccessedByActor(request);
        var validatedRequest = new TilknyttVedleggRequest(tilknyttetAvNavn, accessControlledDocuments.okDocuments());
        tilknyttVedleggRequestValidator.validateRequest(validatedRequest);

        List<FeiledeDokumenter> failedDocuments = accessControlledDocuments.failedDocuments();
        failedDocuments.addAll(tilknyttVedleggTransaksjonService.validateAndTilknyttVedlegg(targetJournalpostId, validatedRequest));
        return failedDocuments;
    }
}
