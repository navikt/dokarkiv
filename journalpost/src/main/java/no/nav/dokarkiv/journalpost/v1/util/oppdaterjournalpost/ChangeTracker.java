package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import lombok.Data;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class ChangeTracker {
    private List<ArkivElementEndringTO> changes;

    private boolean endretFlagg;

    ChangeTracker() {
        changes = new ArrayList<>();
    }

    public void add(ArkivElementEndringTO endring) {
        if(endring != null) {
            if (endring.getFraVerdi() != null) {
                if (endring.getTilVerdi() != null &&
                    !endring.getFraVerdi().equals(endring.getTilVerdi())) {
                    changes.add(endring);
                    endretFlagg = true;
                }
            } else {
                if (endring.getTilVerdi() != null) {
                    changes.add(endring);
                    endretFlagg = true;
                }
            }
        }
    }

    public List<ArkivElementEndringTO> getChanges() {
        return changes.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void add(String arkivElement, String fraVerdi, String tilVerdi) {
        add(ArkivElementEndringTO.builder()
                .arkivElement(arkivElement)
                .fraVerdi(fraVerdi)
                .tilVerdi(tilVerdi)
                .build());
    }
}
