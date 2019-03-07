package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class DokumentInfoMapper {

    private final AksjonsLoggService aksjonsLoggService;

    @Inject
    public DokumentInfoMapper(AksjonsLoggService aksjonsLoggService) {
        this.aksjonsLoggService = aksjonsLoggService;
    }

    public void oppdaterDokumentInfo(DokumentInfo dokumentInfo, String brevkode, String tittel) throws UgyldigAksjonsLoggException {
        boolean endret = false;

        AksjonsLoggHelper aksjonsLoggHelperDokument = new AksjonsLoggHelper();
        aksjonsLoggHelperDokument.setAksjonsLoggTO(AksjonsTypeCode.ENDRE_METADATA, dokumentInfo.getDokumentInfoId());

        if (brevkode != null) {
            dokumentInfo.setBrevkode(brevkode);
            endret = true;
        }
        if (tittel != null) {
            aksjonsLoggHelperDokument.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
                    .arkivElement("DokumentInfo.tittel")
                    .fraVerdi(dokumentInfo.getTittel())
                    .tilVerdi(tittel)
                    .build());
            dokumentInfo.setTittel(tittel);
            endret = true;
        }
        if (endret) {
            dokumentInfo.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
        }

        if (!aksjonsLoggHelperDokument.getArkivElementEndringTOs().isEmpty()) {
            aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelperDokument.getAksjonsLoggTO(), aksjonsLoggHelperDokument
                    .getArkivElementEndringTOs());
        }
    }
}
