package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum JournalpostType {

    INNGAENDE(JournalpostTypeCode.I),
    UTGAAENDE(JournalpostTypeCode.U),
    NOTAT(JournalpostTypeCode.N);

    public final JournalpostTypeCode mapFromValue;

    JournalpostType(JournalpostTypeCode mappedValue) {
        this.mapFromValue = mappedValue;
    }

    public static JournalpostType mapFromJournalpostTypeCode(JournalpostTypeCode journalpostTypeCode) {
        return Arrays.stream(JournalpostType.values())
                .filter(journalpostType -> journalpostType.getMapFromValue() == journalpostTypeCode)
                .findFirst()
                .orElse(null);

    }

}
