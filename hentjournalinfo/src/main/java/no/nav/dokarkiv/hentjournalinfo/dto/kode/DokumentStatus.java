package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;

import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum DokumentStatus {

    FERDIGSTILT(DokumentStatusCode.FERDIGSTILT),
    UNDER_REDIGERING(DokumentStatusCode.UNDER_REDIGERING),
    AVBRUTT(DokumentStatusCode.AVBRUTT);

    private final DokumentStatusCode mapFromValue;

    DokumentStatus(DokumentStatusCode mapFromValue) {
        this.mapFromValue = mapFromValue;
    }

    public static DokumentStatus mapFromDokumentStatusCode(DokumentStatusCode dokumentStatusCode) {
        Optional<DokumentStatus> dokumentStatus = Optional.empty();
        for (DokumentStatus dokumentStatusValue : DokumentStatus.values()) {
            if (dokumentStatusCode == dokumentStatusValue.getMapFromValue()) {
                dokumentStatus = Optional.of(dokumentStatusValue);
            }
        }

        return dokumentStatus.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe DokumentStatusCode=%s til DokumentStatus", dokumentStatusCode)));
    }
}
