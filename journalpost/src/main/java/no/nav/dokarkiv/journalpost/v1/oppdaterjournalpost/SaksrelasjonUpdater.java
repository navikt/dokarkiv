package no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.AksjonsLoggHelper;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.Endret;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class SaksrelasjonUpdater {

    public void updateFields(Journalpost journalpost, OppdaterJournalpostRequest request, AksjonsLoggHelper aksjonsLoggHelper) throws UgyldigAksjonsLoggException {
        Endret endret = new Endret();
        aksjonsLoggHelper.setAksjonsLoggTO(AksjonsTypeCode.SAKSTILKNYTNING);

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

            updateArkivsaksnummer(journalpost, request, saksrelasjon, aksjonsLoggHelper, endret);
            updateArkivsaksystem(journalpost, request, saksrelasjon, aksjonsLoggHelper, endret);

            if (endret.isEndretFlagg() && !newSak) {
                saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_ID));
                saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
            }
            if (newSak) {
                journalpost.setSaksrelasjon(saksrelasjon);
            }
        }
    }

    private void updateArkivsaksystem(Journalpost journalpost, OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, AksjonsLoggHelper aksjonsLoggHelper, Endret endret) {
        if (request.getSak().getArkivsaksystem() != null) {
            aksjonsLoggHelper.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
                    .arkivElement("Saksrelasjon.fagsystem")
                    .fraVerdi(journalpost.getSaksrelasjon().getFagsystem().name())
                    .tilVerdi(request.getSak().getArkivsaksystem().name())
                    .build());
            saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(request.getSak().getArkivsaksystem()));
            endret.setEndretFlagg(true);
        }
    }

    private void updateArkivsaksnummer(Journalpost journalpost, OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, AksjonsLoggHelper aksjonsLoggHelper, Endret endret) {
        if (isNotBlank(request.getSak().getArkivsaksnummer())) {
            aksjonsLoggHelper.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
                    .arkivElement("Saksrelasjon.sakId")
                    .fraVerdi(journalpost.getSaksrelasjon().getSakId())
                    .tilVerdi(request.getSak().getArkivsaksnummer())
                    .build());
            saksrelasjon.setSakId(request.getSak().getArkivsaksnummer());
            endret.setEndretFlagg(true);
        }
    }

    protected FagsystemCode mapArkivSakSystemToFagsystemCode(Arkivsaksystem arkivsaksystem) {
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
