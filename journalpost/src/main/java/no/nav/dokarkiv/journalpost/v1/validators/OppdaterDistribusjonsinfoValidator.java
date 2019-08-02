package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;

import java.util.Arrays;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateBoolean;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class OppdaterDistribusjonsinfoValidator {

    private static final List<JournalStatusCode> ALLOWED_STATES_FOR_DISTRIBUTION = Arrays.asList(FS, FL);

    public void validateRequest(String journalpostId, OppdaterDistribusjonsinfoRequest request) {
        validateId(journalpostId, "journalpostId");
        validateBoolean(request.getSettStatusEkspedert(), "settStatusEkspedert");

        if(isNotBlank(request.getUtsendingsKanal())) {
            try {
                UtsendingsKanalCode.valueOf(request.getUtsendingsKanal());
            } catch (IllegalArgumentException e) {
                throw new KanIkkeOppdatereDistribusjonsinfoException(
                        String.format("Utsendingskanalkode '%s' er ugyldig", request.getUtsendingsKanal()));
            }
        }
    }

    public static void validateJournalpost(Journalpost journalpost) {
        if (!JournalpostTypeCode.U.equals(journalpost.getJournalposttype())) {
            throw new KanIkkeOppdatereDistribusjonsinfoException(
                    String.format("Kan ikke ekspedere journalpost med journalpostType=%s", journalpost.getJournalpostId()));
        }
        if (!ALLOWED_STATES_FOR_DISTRIBUTION.contains(journalpost.getJournalstatus())) {
            throw new KanIkkeOppdatereDistribusjonsinfoException(
                    String.format("Kan ikke ekspedere journalpost med status %s", journalpost.getJournalstatus()));
        }
        if(journalpost.getSaksrelasjon() == null || journalpost.getSaksrelasjon().getFeilregistrert()) {
            throw new KanIkkeOppdatereDistribusjonsinfoException(
                    String.format("Kan ikke ekspedere journalpost med journalpostType=%s", journalpost.getJournalpostId()));
        }
    }

    public static void validateOppdaterteFelt(Journalpost journalpost, OppdaterDistribusjonsinfoRequest request) {
        if(journalpost.getUtsendingskanal() == null && request.getUtsendingsKanal() == null){
            throw new KanIkkeOppdatereDistribusjonsinfoException(
                    String.format("Utsendingskanal er ikke satt, hverken på input eller på journalpost med journalpostType=%s", journalpost.getJournalpostId()));
        }
    }
}
