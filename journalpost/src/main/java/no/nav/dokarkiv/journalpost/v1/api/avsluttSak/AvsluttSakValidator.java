package no.nav.dokarkiv.journalpost.v1.api.avsluttSak;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

import static java.lang.String.format;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class AvsluttSakValidator {

	public static void validateAvsluttSakRequest(AvsluttSakRequest avsluttSakRequest) throws InputValideringFeiletException {
		validateTema(avsluttSakRequest.getTema());
		validateString("fagsakId", avsluttSakRequest.getFagsakId());
		validateString("fagsaksystem", avsluttSakRequest.getFagsaksystem());
		validateBruker(avsluttSakRequest.getBruker());
		validateDate("opprettetDato", avsluttSakRequest.getOpprettetDato());
		if (avsluttSakRequest.getAvsluttetDato() != null)
			validateDate("avsluttetDato", avsluttSakRequest.getAvsluttetDato());
		validateString("administrativEnhet", avsluttSakRequest.getAdministrativEnhet());
	}

	private static void validateDate(String feltnavn, LocalDateTime dato) {
		LocalDateTime naaTid = LocalDateTime.now().plusSeconds(3);
		if (dato == null) {
			throw new InputValideringFeiletException(format("Validering av %s feilet. Dato kan ikke være null", feltnavn));
		} else if (dato.isAfter(naaTid)) {
			throw new InputValideringFeiletException(format("Validering av %s feilet. Dato kan ikke være frem i tid. %s er %s og nåtid er %s",
					feltnavn,
					feltnavn,
					dato,
					naaTid
			));
		}
	}

	private static void validateString(String feltnavn, String feltVerdi) {
		if (StringUtils.isEmpty(feltVerdi)) {
			throw new InputValideringFeiletException(format("Mottok ugyldig verdi for feltet %s. Feltet var null/tomt", feltnavn));
		}
	}

	private static void validateBruker(Bruker bruker) {
		if (bruker == null) {
			throw new InputValideringFeiletException("Bruker kan ikke være null.");
		}
		if (isBlank(bruker.getId())) {
			throw new InputValideringFeiletException("Bruker.id må være satt.");
		}
		if (!isNumeric(bruker.getId())) {
			throw new InputValideringFeiletException("Bruker.id må bestå av tall.");
		}
		if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != 11) {
			throw new InputValideringFeiletException("Bruker.id må være 11 siffer for FNR.");
		} else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != 9) {
			throw new InputValideringFeiletException("Bruker.id må være 9 siffer for ORGNR.");
		} else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != 13) {
			throw new InputValideringFeiletException("Bruker.id må være 13 siffer for AKTOERID.");
		}
	}

	private static void validateTema(String tema) {
		if (StringUtils.isEmpty(tema)) {
			throw new InputValideringFeiletException(format("Mangler påkrevd felt: Tema. Mottok tema=%s", tema));
		}
		try {
			FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Mottatt tema=%s validerer ikke mot kodeverk. Gyldige verdier for tema er %s",
					tema,
					Arrays.toString(FagomradeCode.values())));
		}
	}
}
