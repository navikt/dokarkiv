package no.nav.dokarkiv.journalpost.v1.api.sak;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

import static java.lang.String.format;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class SakRequestValidator {

	private static final int FNR_LENGTH = 11;
	private static final int ORGNR_LENGTH = 9;
	private static final int AKTOERID_LENGTH = 13;

	public static void validateAvsluttSakRequest(AvsluttSakRequest avsluttSakRequest) throws InputValideringFeiletException {
		validateTema(avsluttSakRequest.getTema());
		validateString("fagsakId", avsluttSakRequest.getFagsakId());
		validateString("fagsaksystem", avsluttSakRequest.getFagsaksystem());
		validateBrukerForSak(avsluttSakRequest.getBruker());
		validateDate("opprettetDato", avsluttSakRequest.getOpprettetDato());
		if (avsluttSakRequest.getAvsluttetDato() != null)
			validateDate("avsluttetDato", avsluttSakRequest.getAvsluttetDato());
		validateString("administrativEnhet", avsluttSakRequest.getAdministrativEnhet());
	}

	public static void validateGjenaapneSakRequest(GjenaapneSakRequest gjenaapneSakRequest) throws InputValideringFeiletException {
		validateTema(gjenaapneSakRequest.getTema());
		validateString("fagsakId", gjenaapneSakRequest.getFagsakId());
		validateFagsakSystem(gjenaapneSakRequest.getFagsaksystem());
		validateBrukerForSak(gjenaapneSakRequest.getBruker());
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
		if (StringUtils.isBlank(feltVerdi)) {
			throw new InputValideringFeiletException(format("Mottok ugyldig verdi for feltet %s. Feltet var null/tomt", feltnavn));
		}
	}

	public static void validateBrukerForSak(Bruker bruker) {
		if (bruker == null) {
			throw new InputValideringFeiletException("bruker kan ikke være null.");
		}

		if (bruker.getIdType() == null) {
			throw new InputValideringFeiletException("bruker.idType må være satt");
		}

		if (isBlank(bruker.getId())) {
			throw new InputValideringFeiletException("bruker.id må være satt.");
		}
		if (!isNumeric(bruker.getId())) {
			throw new InputValideringFeiletException("bruker.id må bestå av tall.");
		}
		if (bruker.getIdType() == null) {
			throw new InputValideringFeiletException("bruker.idType må være satt.");
		}

		if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
			throw new InputValideringFeiletException("bruker.id må være 11 siffer dersom bruker.idType=FNR.");
		} else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
			throw new InputValideringFeiletException("bruker.id må være 9 siffer dersom bruker.idType=ORGNR.");
		} else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
			throw new InputValideringFeiletException("bruker.id må være 13 siffer dersom bruker.idType=AKTOERID.");
		}
	}

	private static void validateFagsakSystem(String fagsaksystem) {
		if (StringUtils.isBlank(fagsaksystem)) {
			throw new InputValideringFeiletException(format("Mangler påkrevd felt: fagsaksystem. Mottok fagsaksystem=%s", fagsaksystem));
		}
		try {
			Fagsaksystem.valueOf(fagsaksystem);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Mottatt fagsaksystem=%s validerer ikke mot kodeverk. Gyldige verdier for fagsaksystem er %s",
					fagsaksystem,
					Arrays.toString(Fagsaksystem.values())));
		}
	}

	private static void validateTema(String tema) {
		if (StringUtils.isBlank(tema)) {
			throw new InputValideringFeiletException(format("Mangler påkrevd felt: tema. Mottok tema=%s", tema));
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
