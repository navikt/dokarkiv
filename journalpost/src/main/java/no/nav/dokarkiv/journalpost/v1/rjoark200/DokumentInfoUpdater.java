package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.rjoark200.util.Endret;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class DokumentInfoUpdater {

    private final AksjonsLoggService aksjonsLoggService;

    @Inject
    public DokumentInfoUpdater(AksjonsLoggService aksjonsLoggService) {
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public void updateFields(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest) throws UgyldigAksjonsLoggException {

        Endret endret = new Endret();
        AksjonsLoggHelper aksjonsLoggHelperDokument = new AksjonsLoggHelper();
        aksjonsLoggHelperDokument.setAksjonsLoggTO(AksjonsTypeCode.ENDRE_METADATA, dokumentJoark.getDokumentInfoId());

        updateBrevkode(dokumentJoark, dokumentRequest, endret);
        updateTittel(dokumentJoark, dokumentRequest, aksjonsLoggHelperDokument, endret);

        if (endret.isEndret()) {
            dokumentJoark.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentJoark.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
        }

        if (!aksjonsLoggHelperDokument.getArkivElementEndringTOs().isEmpty()) {
            aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelperDokument.getAksjonsLoggTO(), aksjonsLoggHelperDokument
                    .getArkivElementEndringTOs());
        }
    }

    private void updateTittel(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest, AksjonsLoggHelper aksjonsLoggHelperDokument, Endret endret) {
        if (dokumentRequest.getTittel() != null) {
            aksjonsLoggHelperDokument.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
                    .arkivElement("DokumentInfo.tittel")
                    .fraVerdi(dokumentJoark.getTittel())
                    .tilVerdi(dokumentRequest.getTittel())
                    .build());
            dokumentJoark.setTittel(dokumentRequest.getTittel());
            endret.setEndret(true);
        }
    }

    private void updateBrevkode(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest, Endret endret) {
        if (dokumentRequest.getBrevkode() != null) {
            dokumentJoark.setBrevkode(dokumentRequest.getBrevkode());
            endret.setEndret(true);
        }
    }
}
