package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import no.nav.service.dok.joark.nsb.KnyttDokumentTilJournalpostSomVedleggValidator;
import no.nav.service.dok.joark.nsb.to.KnyttDokumentTilJournalpostSomVedleggRequestTo;
import org.springframework.util.Assert;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DefaultKnyttDokumentTilJournalpostSomVedleggValidator implements KnyttDokumentTilJournalpostSomVedleggValidator {

    @Override
    public void validate(KnyttDokumentTilJournalpostSomVedleggRequestTo request) {
        Assert.notNull(request, "Missing request object");
        Assert.isTrue(request.getKnyttesFraJournalpostId() > 0, "Missing parameter in request: knyttesFraJournalpostId");
        Assert.isTrue(request.getKnyttesTilJournalpostId() > 0, "Missing parameter in request: knyttesTilJournalpostId");
        Assert.isTrue(request.getDokumentInfoId() > 0, "Missing parameter in request: dokumentInfoId");
        Assert.hasLength(request.getEndretAvNavn(), "Missing parameter in request: endretAvNavn");
    }
}
