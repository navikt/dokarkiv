package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsluttAlleSakerPaaTemaRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class AvsluttAlleSakerPaaTemaValidator {

	public static void validerAvsluttAlleSakerPaaTemaRequest(AvsluttAlleSakerPaaTemaRequest request) {
		validerTema(request.tema());
		validerReferanse(request.referanse());
		validerAvsluttetDato(request.avsluttetDato());
		validerAdministrativEnhet(request.administrativEnhet());
	}

	private static void validerTema(String tema) {
		if (isBlank(tema)) {
			throw new InputValideringFeiletException("tema kan ikke være null eller tom");
		}

		try {
			FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(
					format("tema=%s validerer ikke mot kodeverk. Gyldige verdier for tema er %s", tema, Arrays.toString(FagomradeCode.values()))
			);
		}
	}

	private static void validerReferanse(String referanse) {
		if (isBlank(referanse)) {
			throw new InputValideringFeiletException("referanse kan ikke være null eller tom");
		}

		if (referanse.length() > 40) {
			throw new InputValideringFeiletException("referanse kan ikke være lengre enn 40 tegn. Mottok=%s".formatted(referanse));
		}
	}

	private static void validerAvsluttetDato(LocalDateTime avsluttetDato) {
		if (avsluttetDato != null) {
			LocalDateTime naatid = LocalDateTime.now().plusSeconds(3);

			if (avsluttetDato.isAfter(naatid)) {
				throw new InputValideringFeiletException("avsluttetDato kan ikke være i fremtiden. Nåtid er=%s og mottok=%s".formatted(naatid, avsluttetDato));
			}
		}
	}

	private static void validerAdministrativEnhet(String administrativEnhet) {
		if (administrativEnhet != null) {
			if (!StringUtils.isNumeric(administrativEnhet)) {
				throw new InputValideringFeiletException("administrativEnhet må være et heltall. Mottok=%s".formatted(administrativEnhet));
			}

			if (administrativEnhet.length() != 4) {
				throw new InputValideringFeiletException("administrativEnhet må ha en lengde på 4. Mottok=%s".formatted(administrativEnhet));
			}
		}
	}

}