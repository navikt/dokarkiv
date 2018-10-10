package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;

import java.util.Arrays;

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
        return Arrays.stream(DokumentStatus.values())
                .filter(dokumentStatus -> dokumentStatus.getMapFromValue() == dokumentStatusCode)
                .findFirst()
                .orElse(null);
    }
}
