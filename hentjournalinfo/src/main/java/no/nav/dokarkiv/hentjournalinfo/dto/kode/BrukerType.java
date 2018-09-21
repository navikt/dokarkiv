package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

import java.util.Optional;

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
        Optional<BrukerType> brukerType = Optional.empty();
        for (BrukerType brukerTypeValue : BrukerType.values()) {
            if (brukerTypeCode == brukerTypeValue.getMapFromValue()) {
                brukerType = Optional.of(brukerTypeValue);
            }
        }

        return brukerType.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe BrukerTypeCode=%s til BrukerType", brukerTypeCode)));
    }
}
