package no.nav.dokarkiv.journalpost.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AksjonsLoggHelper {

    private final AksjonsLoggTOMapper aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
    private AksjonsLoggTO aksjonsLoggTO;
    private ArrayList<ArkivElementEndringTO> arkivElementEndringTOs = new ArrayList<>();

    private static long journalpostId;
    private static String aksjonsLoggHeaderString;
    private static String brukerId;

    public void setAksjonsLoggTO(AksjonsTypeCode aksjonsTypeCode, Long dokumentInfoId) throws UgyldigAksjonsLoggException {
        if (isBlank(aksjonsLoggHeaderString)) {
            this.aksjonsLoggTO = AksjonsLoggTO.builder()
                    .aksjon(aksjonsTypeCode)
                    .journalpostId(journalpostId)
                    .utfoertAv(MDC.get(MDC_CONSUMER_ID))
                    .bruker(brukerId)
                    .dokumentInfoId(dokumentInfoId)
                    .melding(aksjonsTypeCode.equals(AksjonsTypeCode.SAKSTILKNYTNING) ?
                            "Journalposten ble knyttet til en sak." :
                            "Metadata på journalposten ble endretFlagg.")
                    .build();
        } else {
            this.aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, aksjonsTypeCode, journalpostId, dokumentInfoId);
        }
    }

    public void setAksjonsLoggTO(AksjonsTypeCode aksjonsTypeCode) throws UgyldigAksjonsLoggException {
        setAksjonsLoggTO(aksjonsTypeCode, null);
    }

    public void addToArkivElementEndringTOs(ArkivElementEndringTO arkivElementEndringTO) {
        if(arkivElementEndringTO.getFraVerdi() == null
                || !arkivElementEndringTO.getFraVerdi().equals(arkivElementEndringTO.getTilVerdi())) {
            this.arkivElementEndringTOs.add(arkivElementEndringTO);
        }
    }

    public static void setJournalpostId(long journalpostId) {
        AksjonsLoggHelper.journalpostId = journalpostId;
    }

    public static void setAksjonsLoggHeaderString(String aksjonsLoggHeaderString) {
        AksjonsLoggHelper.aksjonsLoggHeaderString = aksjonsLoggHeaderString;
    }

    public static void setBrukerId(String brukerId) {
        AksjonsLoggHelper.brukerId = brukerId;
    }

    public AksjonsLoggTO getAksjonsLoggTO() {
        return aksjonsLoggTO;
    }

    public List<ArkivElementEndringTO> getArkivElementEndringTOs() {
        return new ArrayList<>(arkivElementEndringTOs);
    }
}