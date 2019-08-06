package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class DokumentInfoUpdater {

    public ChangeTracker updateFields(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest) {

        ChangeTracker tracker = new ChangeTracker();

        updateBrevkode(dokumentJoark, dokumentRequest, tracker);
        updateTittel(dokumentJoark, dokumentRequest, tracker);

        if (tracker.isEndretFlagg()) {
            dokumentJoark.setEndretAvNavn(MDC.get(MDC_USER_ID));
            dokumentJoark.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
        }
        return tracker;
    }

    private void updateTittel(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest, ChangeTracker endret) {
        if (dokumentRequest.getTittel() != null && !dokumentRequest.getTittel().equals(dokumentJoark.getTittel())) {
            endret.add("DokumentInfo.tittel", dokumentJoark.getTittel(), dokumentRequest.getTittel());
            dokumentJoark.setTittel(dokumentRequest.getTittel());
        }
    }

    private void updateBrevkode(DokumentInfo dokumentJoark, no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest, ChangeTracker endret) {
        if (dokumentRequest.getBrevkode() != null && !dokumentRequest.getBrevkode().equals(dokumentJoark.getBrevkode())) {
            endret.add("DokumentInfo.brevkode", dokumentJoark.getBrevkode(), dokumentRequest.getBrevkode());
            dokumentJoark.setBrevkode(dokumentRequest.getBrevkode());
        }
    }
}
