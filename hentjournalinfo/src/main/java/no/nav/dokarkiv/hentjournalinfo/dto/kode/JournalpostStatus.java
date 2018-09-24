package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum JournalpostStatus {

    @GraphQLEnumValue(description = "Endelig journalføring")
    JOURNALFOERT(JournalStatusCode.J),

    @GraphQLEnumValue(description = "Midlertidig journalføring")
    MIDLERTIDIG_JOURNALFOERT(JournalStatusCode.M),

    @GraphQLEnumValue(description = "Ferdigstilt og sendt videre til sentralprint")
    FERDIGSTILT_SENTRALPRINT(JournalStatusCode.FS),

    @GraphQLEnumValue(description = "Ferdigstilt og sendt videre til lokalprint")
    FERDIGSTILT_LOKALPRINT(JournalStatusCode.FL),

    @GraphQLEnumValue(description = "Utgår")
    UTGAAR(JournalStatusCode.U),

    @GraphQLEnumValue(description = "Avbrutt")
    AVBRUTT(JournalStatusCode.A),

    @GraphQLEnumValue(description = "Dokument under produksjon")
    UNDER_PRODUKSJON(JournalStatusCode.D),

    @GraphQLEnumValue(description = "Ekspedert")
    EKSPEDERT(JournalStatusCode.E),

    @GraphQLEnumValue(description = "Mottat")
    MOTTAT(JournalStatusCode.MO),

    @GraphQLEnumValue(description = "Ukjent bruker")
    UKJENT_BRUKER(JournalStatusCode.UB),

    @GraphQLEnumValue(description = "Opplaster dokument")
    OPPLASTER_DOKUMENT(JournalStatusCode.OD),

    @GraphQLEnumValue(description = "Reservert dokument")
    RESERVERT(JournalStatusCode.R);

    private final JournalStatusCode mapFromValue;

    JournalpostStatus(JournalStatusCode mapFromValue) {
        this.mapFromValue = mapFromValue;
    }

    public static JournalpostStatus mapFromJournalStatusCode(JournalStatusCode journalStatusCode) {
        return Arrays.stream(JournalpostStatus.values())
                .filter(journalStatus -> journalStatus.getMapFromValue() == journalStatusCode)
                .findFirst()
                .orElse(null);
    }


}
