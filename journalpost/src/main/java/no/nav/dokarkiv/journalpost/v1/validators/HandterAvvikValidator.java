package no.nav.dokarkiv.journalpost.v1.validators;

import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.AVBRYT;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.FEILREGISTRER_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_UKJENT_BRUKER;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;

import java.util.Arrays;
import java.util.List;

public class HandterAvvikValidator {

    private static final List<String> validAvvikstyper = Arrays.asList(FEILREGISTRER_SAKSTILKNYTNING, OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING, SETT_UKJENT_BRUKER, AVBRYT);

    public static void validateAvvikstype(String avvikstype) {
        if (!validAvvikstyper.contains(avvikstype)) {
            throw new InputValideringFeiletException("Ugyldig avvikstype");
        }
    }
}
