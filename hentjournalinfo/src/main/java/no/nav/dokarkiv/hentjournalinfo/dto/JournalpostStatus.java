package no.nav.dokarkiv.hentjournalinfo.dto;

import static java.lang.String.format;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum JournalpostStatus {

    JOURNALFOERT(JournalStatusCode.J),

    MIDLERTIDIG_JOURNALFOERT(JournalStatusCode.M),

    FERDIGSTILT_SENTRALPRINT(JournalStatusCode.FS),

    FERDIGSTILT_LOKALPRINT(JournalStatusCode.FL),

    UTGAAR(JournalStatusCode.U),

    AVBRUTT(JournalStatusCode.A),

    UNDER_PRODUKSJON(JournalStatusCode.D),

    EKSPEDERT(JournalStatusCode.E),

    MOTTAT(JournalStatusCode.MO),

    UKJENT_BRUKER(JournalStatusCode.UB),

    OPPLASTER_DOKUMENT(JournalStatusCode.OD),

    RESERVERT(JournalStatusCode.R);

    private final JournalStatusCode mapFromValue;

    JournalpostStatus(JournalStatusCode mapFromValue) {
        this.mapFromValue = mapFromValue;
    }

    public static JournalpostStatus mapFromJournalStatusCode(JournalStatusCode journalStatusCode) {
        Optional<JournalpostStatus> journalpostType = Optional.empty();
        for (JournalpostStatus journalpostStatusValue : JournalpostStatus.values()) {
            if (journalStatusCode == journalpostStatusValue.getMapFromValue()) {
                journalpostType = Optional.of(journalpostStatusValue);
            }
        }

        return journalpostType.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe JournalStatusCode=%s til JournalpostStatus", journalStatusCode)));
    }


}
