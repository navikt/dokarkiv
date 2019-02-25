package no.nav.dokarkiv.oppdatermetadata.v1.temp001;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class DokumentInfoMapper {

    public void oppdaterDokumentInfo(no.nav.dok.oppdatermetadata.api.v1.DokumentInfo request, DokumentInfo dokumentInfo) {
        boolean endret = false;
        if (isNotBlank(request.getBrevkode())) {
            dokumentInfo.setBrevkode(request.getBrevkode());
            endret = true;
        }
        if (isNotBlank(request.getTittel())) {
            dokumentInfo.setTittel(request.getTittel());
            endret = true;
        }
        if (endret) {
            dokumentInfo.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
        }
    }

}
