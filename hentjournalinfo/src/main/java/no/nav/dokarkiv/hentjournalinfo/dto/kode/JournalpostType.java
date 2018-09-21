package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.Optional;

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
        Optional<JournalpostType> journalpostType = Optional.empty();
        for (JournalpostType journalpostTypeValue : JournalpostType.values()) {
            if (journalpostTypeCode == journalpostTypeValue.getMapFromValue()) {
                journalpostType = Optional.of(journalpostTypeValue);
            }
        }

        return journalpostType.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe JournalpostTypeCode=%s til JournalpostType", journalpostTypeCode)));
    }

}
