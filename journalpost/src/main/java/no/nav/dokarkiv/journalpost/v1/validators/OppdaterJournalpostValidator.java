package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public final class OppdaterJournalpostValidator {
    private static final int FNR_LENGTH = 11;
    private static final int AKTOERID_LENGTH = 13;
    private static final int ORGNR_LENGTH = 9;

    private static final List<JournalStatusCode> restrictedJournalpostStatusCodes = Arrays.asList(J, FS, FL, E);

    private OppdaterJournalpostValidator() {
    }

    public static void validateOppdaterteFelt(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {

        if (restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
            checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpostStatus, journalpostType);
            checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpostStatus, journalpostType);
            checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpostStatus, journalpostType);
            checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpostStatus, journalpostType);

            if (!J.equals(journalpostStatus) && !N.equals(journalpostType)) {
                checkIfIllegalFieldIsSet(request.getTittel(), "Tittel", journalpostStatus, journalpostType);
                if (request.getAvsenderMottaker() != null) {
                    checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getId(), "AvsendeMottakerId", journalpostStatus, journalpostType);
                    checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getIdType(), "AvsendeMottakerIdType", journalpostStatus, journalpostType);
                    checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getNavn(), "AvsendeMottakerNavn", journalpostStatus, journalpostType);
                }
            }
        } else if (request.getSak() != null) {
            validateSak(request.getSak(), request.getBruker(), request.getTema());
        }

        if (journalpostType != JournalpostTypeCode.U || !restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
            checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType);
        }

        if (isNotBlank(request.getBehandlingstema())) {
            validateBehandlingstema(request.getBehandlingstema());
        }
    }

    private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
        if (field != null) {
            throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpostStatus=%s og journalpostType=%s.", fieldName, journalpoststatus
                    .name(), journalpostType.name()));
        }
    }

    private static void validateSak(Sak sak, Bruker bruker, String tema) {
        if (FAGSAK.equals(sak.getSakstype())) {
            validateFagsak(sak, bruker, tema);
        }

        if (GENERELL_SAK.equals(sak.getSakstype())) {
            validateGenerellSak(sak, bruker, tema);
        }

        if (ARKIVSAK.equals(sak.getSakstype()) || sak.getSakstype() == null) {
            validateArkivsak(sak);
        }
    }

    private static void validateFagsak(Sak sak, Bruker bruker, String tema) {
        if (isBlank(tema)) {
            throw new InputValideringFeiletException("tema må være satt dersom sakstype=FAGSAK");
        }
        if (isBrukerNull(bruker)) {
            throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=FAGSAK");
        }
        validateBruker(bruker);

        if (isBlank(sak.getFagsakId())) {
            throw new InputValideringFeiletException("Sak.fagsakId må være satt dersom sakstype=FAGSAK");
        }
        if (sak.getFagsaksystem() == null) {
            throw new InputValideringFeiletException("Sak.fagsaksystem må være satt dersom sakstype=FAGSAK");
        }
        if (isNotBlank(sak.getArkivsaksnummer())) {
            throw new InputValideringFeiletException("Sak.arkivsaksnummer skal ikke være satt dersom sakstype=FAGSAK");
        }
        if (sak.getArkivsaksystem() != null) {
            throw new InputValideringFeiletException("Sak.arkivsaksystem skal ikke være satt dersom sakstype=FAGSAK");
        }
    }

    private static void validateGenerellSak(Sak sak, Bruker bruker, String tema) {
        if (isBlank(tema)) {
            throw new InputValideringFeiletException("tema må være satt dersom sakstype=GENERELL_SAK");
        }
        if (isBrukerNull(bruker)) {
            throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=GENERELL_SAK");
        }
        validateBruker(bruker);

        if (isNotBlank(sak.getFagsakId())) {
            throw new InputValideringFeiletException("Sak.fagsakId skal ikke være satt dersom sakstype=GENERELL_SAK");
        }
        if (sak.getFagsaksystem() != null) {
            throw new InputValideringFeiletException("Sak.fagsaksystem skal ikke være satt dersom sakstype=GENERELL_SAK");
        }
        if (isNotBlank(sak.getArkivsaksnummer())) {
            throw new InputValideringFeiletException("Sak.arkivsaksnummer skal ikke være satt dersom sakstype=GENERELL_SAK");
        }
        if (sak.getArkivsaksystem() != null) {
            throw new InputValideringFeiletException("Sak.arkivsaksystem skal ikke være satt dersom sakstype=GENERELL_SAK");
        }
    }

    private static void validateArkivsak(Sak sak) {
        if (isNotBlank(sak.getFagsakId())) {
            throw new InputValideringFeiletException("Sak.fagsakId skal ikke være satt dersom sakstype=ARKIVSAK");
        }
        if (sak.getFagsaksystem() != null) {
            throw new InputValideringFeiletException("Sak.fagsaksystem skal ikke være satt dersom sakstype=ARKIVSAK");
        }
        if (isBlank(sak.getArkivsaksnummer())) {
            throw new InputValideringFeiletException("Sak.arkivsaksnummer må være satt dersom sakstype=GENERELL_SAK");
        }
        if (sak.getArkivsaksystem() == null) {
            throw new InputValideringFeiletException("Sak.arkivsaksystem må være satt dersom sakstype=GENERELL_SAK");
        }
        if (!isNumeric(sak.getArkivsaksnummer())) {
            throw new InputValideringFeiletException("Sak.arkivsaksnummer skal være opprettet i GSAK/PSAK og må være et numerisk heltall.");
        }
    }

    private static boolean isBrukerNull(Bruker bruker) {
        return isBlank(bruker.getId()) || Objects.isNull(bruker.getIdType());
    }

    private static void validateBehandlingstema(String behandlingstema) {
        if (behandlingstema.length() != 6 || !behandlingstema.startsWith("ab")) {
            throw new InputValideringFeiletException(String.format("Behandlingstema er ikke på formatet ´ab + 4 siffer´. Behandlingstema er=%s", behandlingstema));
        }
    }

    private static void validateBruker(Bruker bruker) {
        if (isBlank(bruker.getId())) {
            throw new InputValideringFeiletException("Bruker.id må være satt.");
        }
        if (!isNumeric(bruker.getId())) {
            throw new InputValideringFeiletException("Bruker.id må bestå av tall.");
        }
        if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
            throw new InputValideringFeiletException("Bruker.id må være 11 siffer for FNR.");
        } else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
            throw new InputValideringFeiletException("Bruker.id må være 9 siffer for ORGNR.");
        } else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
            throw new InputValideringFeiletException("Bruker.id må være 11 siffer for AKTOERID.");
        }
    }
}
