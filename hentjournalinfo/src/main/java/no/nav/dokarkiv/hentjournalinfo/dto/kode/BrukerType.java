package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum BrukerType {

    PERSON(BrukerTypeCode.PERSON),
    ORGANISASJON(BrukerTypeCode.ORGANISASJON),
    SAMHANDLER(BrukerTypeCode.SAMHANDLER);

    private final BrukerTypeCode mapFromValue;

    BrukerType(BrukerTypeCode mapFromValue) {
        this.mapFromValue = mapFromValue;
    }

    public static BrukerType mapFromBrukerTypeCode(BrukerTypeCode brukerTypeCode) {
        return Arrays.stream(BrukerType.values())
                .filter(brukerType -> brukerType.getMapFromValue() == brukerTypeCode)
                .findFirst()
                .orElse(null);
    }
}
