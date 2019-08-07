package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class SaksrelasjonUpdater {

    public ChangeTracker updateFields(Journalpost journalpost, OppdaterJournalpostRequest request) {
        ChangeTracker endret = new ChangeTracker();
        boolean newSak = false;

        if (request.getSak() != null) {
            Saksrelasjon saksrelasjon;

            if (journalpost.getSaksrelasjon() == null) {
                saksrelasjon = new Saksrelasjon();
                saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
                newSak = true;
            } else {
                saksrelasjon = journalpost.getSaksrelasjon();
            }

            updateArkivsaksnummer(journalpost, request, saksrelasjon, endret);
            updateArkivsaksystem(journalpost, request, saksrelasjon, endret);

            if (endret.isEndretFlagg() && !newSak) {
                saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_ID));
                saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
            }
            if (newSak) {
                journalpost.setSaksrelasjon(saksrelasjon);
            }
        }
        return endret;
    }

    private void updateArkivsaksystem(Journalpost journalpost, OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, ChangeTracker endret) {
        if (request.getSak().getArkivsaksystem() != null &&
                !mapArkivSakSystemToFagsystemCode(request.getSak().getArkivsaksystem()).equals(journalpost.getSaksrelasjon().getFagsystem())) {
            endret.add("Saksrelasjon.fagsystem", journalpost.getSaksrelasjon() == null ? null : journalpost.getSaksrelasjon().getFagsystem().name(),
                    request.getSak().getArkivsaksystem().name());
            saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(request.getSak().getArkivsaksystem()));
        }
    }

    private void updateArkivsaksnummer(Journalpost journalpost, OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, ChangeTracker endret) {
        if (isNotBlank(request.getSak().getArkivsaksnummer()) && !request.getSak().getArkivsaksnummer().equals(journalpost.getSaksrelasjon().getSakId())) {
            endret.add("Saksrelasjon.sakId", journalpost.getSaksrelasjon() == null ? null : journalpost.getSaksrelasjon().getSakId(),
                    request.getSak().getArkivsaksnummer());
            saksrelasjon.setSakId(request.getSak().getArkivsaksnummer());
        }
    }

    FagsystemCode mapArkivSakSystemToFagsystemCode(Arkivsaksystem arkivsaksystem) {
        assertNotNull(arkivsaksystem, "arkivsaksystem");
        if (Arkivsaksystem.GSAK.equals(arkivsaksystem)) {
            return FagsystemCode.FS22;
        } else {
            return FagsystemCode.PEN;
        }
    }

    private void assertNotNull(Object object, String fieldName) {
        if (object == null) {
            throw new InputValideringFeiletException(String.format("%s kan ikke være null", fieldName));
        }
    }
}
