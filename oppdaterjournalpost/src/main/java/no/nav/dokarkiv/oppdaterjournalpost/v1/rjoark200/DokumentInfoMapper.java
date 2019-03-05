package no.nav.dokarkiv.oppdaterjournalpost.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.oppdaterjournalpost.v1.support.AksjonsloggHelper;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class DokumentInfoMapper {

    @Inject
    AksjonsloggHelper aksjonsloggHelper;

    public void oppdaterDokumentInfo(DokumentInfo dokumentInfo, String brevkode, String tittel) throws UgyldigAksjonsLoggException {
        boolean endret = false;

        AksjonsloggHelper aksjonsloggHelperDokument = new AksjonsloggHelper();
        aksjonsloggHelperDokument.setAksjonsLoggTO(AksjonsTypeCode.ENDRE_METADATA, dokumentInfo.getDokumentInfoId());

        if (brevkode != null) {
            dokumentInfo.setBrevkode(brevkode);
            endret = true;
        }
        if (tittel != null) {
            aksjonsloggHelperDokument.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
                    .arkivElement("DokumentInfo.tittel")
                    .fraVerdi(dokumentInfo.getTittel())
                    .tilVerdi(tittel)
                    .build());
            dokumentInfo.setTittel(tittel);
            aksjonsloggHelperDokument.populerAksjonslogg();
            endret = true;
        }
        if (endret) {
            dokumentInfo.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
        }
    }
}
