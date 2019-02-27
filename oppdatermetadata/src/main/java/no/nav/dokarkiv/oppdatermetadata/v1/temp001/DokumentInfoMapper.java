package no.nav.dokarkiv.oppdatermetadata.v1.temp001;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class DokumentInfoMapper {

    public void oppdaterDokumentInfo(DokumentInfo dokumentInfo, String brevkode, String tittel) {
        boolean endret = false;
        if (brevkode != null) {
            dokumentInfo.setBrevkode(brevkode);
            endret = true;
        }
        if (tittel != null) {
            dokumentInfo.setTittel(tittel);
            endret = true;
        }
        if (endret) {
            dokumentInfo.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
        }
    }

}
