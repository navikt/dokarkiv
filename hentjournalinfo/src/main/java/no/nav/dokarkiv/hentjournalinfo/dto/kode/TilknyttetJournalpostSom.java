package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

import java.util.Optional;

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
        Optional<TilknyttetJournalpostSom> tilknyttetJournalpostSom = Optional.empty();
        for (TilknyttetJournalpostSom tilknyttetJournalpostSomValue : TilknyttetJournalpostSom.values()) {
            if (tilknyttetJournalpostSomCode == tilknyttetJournalpostSomValue.getMapFromValue()) {
                tilknyttetJournalpostSom = Optional.of(tilknyttetJournalpostSomValue);
            }
        }

        return tilknyttetJournalpostSom.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe TilknyttetJournalpostSomCode=%s til TilknyttetJournalpostSom", tilknyttetJournalpostSomCode)));
    }
}
