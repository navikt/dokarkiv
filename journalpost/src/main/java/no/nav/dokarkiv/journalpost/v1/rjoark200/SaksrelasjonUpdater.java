package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.rjoark200.util.Utils.assertNotNull;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark200.util.Endret;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class SaksrelasjonUpdater {
    private final AksjonsLoggService aksjonsLoggService;

    @Inject
    public SaksrelasjonUpdater(AksjonsLoggService aksjonsLoggService) {
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public void updateFields(Journalpost journalpost, OppdaterJournalpostRequest request) throws UgyldigAksjonsLoggException {
        Endret endret = new Endret();
        AksjonsLoggHelper aksjonsLoggHelper = new AksjonsLoggHelper();
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

            if (endret.isEndret() && !newSak) {
                saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_ID));
                saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
            }
            if (newSak) {
                journalpost.setSaksrelasjon(saksrelasjon);
            }

            if (!aksjonsLoggHelper.getArkivElementEndringTOs().isEmpty()) {
                aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelper.getAksjonsLoggTO(), aksjonsLoggHelper
                        .getArkivElementEndringTOs());
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
            endret.setEndret(true);
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
            endret.setEndret(true);
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
}
