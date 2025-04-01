package no.nav.dokarkiv.journalpost.v1.api.knytttilannensak;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnyttTilAnnenSakResponse {

    private final Long nyJournalpostId;

    @JsonCreator
    public KnyttTilAnnenSakResponse(@JsonProperty("nyJournalpostId") Long nyJournalpostId){
        this.nyJournalpostId = nyJournalpostId;
    }
}
