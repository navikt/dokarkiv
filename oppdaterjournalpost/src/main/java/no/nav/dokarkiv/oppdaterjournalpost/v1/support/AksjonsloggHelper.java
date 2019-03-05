package no.nav.dokarkiv.oppdaterjournalpost.v1.support;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class AksjonsloggHelper {

    @Inject
    private AksjonsLoggService aksjonsLoggService;

    private final AksjonsLoggTOMapper aksjonsLoggTOMapper = new AksjonsLoggTOMapper();

    private AksjonsLoggTO aksjonsLoggTO;
    private List<ArkivElementEndringTO> arkivElementEndringTOs;

    private static long journalpostId;
    private static String aksjonsLoggHeaderString;
    private static String brukerId;

    public AksjonsloggHelper() {
    }

    public void populerAksjonslogg() {
        if (!this.arkivElementEndringTOs.isEmpty()) {
            try {
                aksjonsLoggService.validateAndSaveAksjonsLogg(this.aksjonsLoggTO, this.arkivElementEndringTOs);
            } catch (UgyldigAksjonsLoggException e) {
                log.error("");
            }
        }
    }

    public void setAksjonsLoggTO(AksjonsTypeCode aksjonsTypeCode, Long dokumentInfoId) {
        if (isBlank(this.aksjonsLoggHeaderString)) {
            this.aksjonsLoggTO = AksjonsLoggTO.builder()
                    .aksjon(aksjonsTypeCode)
                    .journalpostId(this.journalpostId)
                    .utfoertAv(MDC.get(MDC_CONSUMER_ID))
                    .bruker(this.brukerId)
                    .dokumentInfoId(dokumentInfoId)
                    .melding(aksjonsTypeCode.equals(AksjonsTypeCode.SAKSTILKNYTNING) ?
                            "Journalposten ble knyttet til en sak." :
                            "Metadata på journalposten ble endret.")
                    .build();
        } else {
            try {
                this.aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(this.aksjonsLoggHeaderString, aksjonsTypeCode, this.journalpostId, dokumentInfoId);
            } catch (UgyldigAksjonsLoggException e) {
                log.error("");
            }
        }
    }

    public void addToArkivElementEndringTOs(ArkivElementEndringTO arkivElementEndringTO) {
        this.arkivElementEndringTOs.add(arkivElementEndringTO);
    }

    public static void setJournalpostId(long journalpostId) {
        AksjonsloggHelper.journalpostId = journalpostId;
    }

    public static void setAksjonsLoggHeaderString(String aksjonsLoggHeaderString) {
        AksjonsloggHelper.aksjonsLoggHeaderString = aksjonsLoggHeaderString;
    }

    public static void setBrukerId(String brukerId) {
        AksjonsloggHelper.brukerId = brukerId;
    }
}