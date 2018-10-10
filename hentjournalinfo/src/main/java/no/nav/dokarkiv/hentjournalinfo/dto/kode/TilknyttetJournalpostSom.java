package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum TilknyttetJournalpostSom {

    HOVEDDOKUMENT(TilknyttetJournalpostSomCode.HOVEDDOKUMENT),
    VEDLEGG(TilknyttetJournalpostSomCode.VEDLEGG),
    @GraphQLEnumValue(description = "Sammensatt dokument")
    SAMMENSATT_DOKUMENT(TilknyttetJournalpostSomCode.SAMMENSATT_DOK);

    TilknyttetJournalpostSom(TilknyttetJournalpostSomCode mapFromValue) {
        this.mapFromValue = mapFromValue;
    }

    private final TilknyttetJournalpostSomCode mapFromValue;

    public static TilknyttetJournalpostSom mapTilknyttetJournalpostSomCode(TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
        return Arrays.stream(TilknyttetJournalpostSom.values())
                .filter(tilknyttetJournalpostSom -> tilknyttetJournalpostSom.getMapFromValue() == tilknyttetJournalpostSomCode)
                .findFirst()
                .orElse(null);
    }
}
