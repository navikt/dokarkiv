package no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.AksjonsLoggHelper;
import no.nav.dokarkiv.journalpost.v1.util.Endret;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class DokumentInfoUpdater {

    public void updateFields(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest, AksjonsLoggHelper aksjonsLoggHelper) throws UgyldigAksjonsLoggException {

        Endret endret = new Endret();
        aksjonsLoggHelper.setAksjonsLoggTO(AksjonsTypeCode.ENDRE_METADATA, dokumentJoark.getDokumentInfoId());

        updateBrevkode(dokumentJoark, dokumentRequest, endret);
        updateTittel(dokumentJoark, dokumentRequest, aksjonsLoggHelper, endret);

        if (endret.isEndretFlagg()) {
            dokumentJoark.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentJoark.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
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
            endret.setEndretFlagg(true);
        }
    }

    private void updateBrevkode(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest, Endret endret) {
        if (dokumentRequest.getBrevkode() != null) {
            dokumentJoark.setBrevkode(dokumentRequest.getBrevkode());
            endret.setEndretFlagg(true);
        }
    }
}
