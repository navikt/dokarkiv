package no.nav.dokarkiv.logiskslettdokument.common;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import org.slf4j.MDC;

public class SlettemeldingsFunksjoner {

	private static final String SLETTEMELDING = " - slettet";

	public static String getSlettemelding() {
		return SLETTEMELDING;
	}

	public static DokumentInfo setDokumentLogiskSlettet(DokumentInfo dokumentInfo) {
		dokumentInfo.setSlettet(true);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		dokumentInfo.setTittel(setSlettemelding(dokumentInfo.getTittel()));
		return dokumentInfo;
	}

	public static DokumentInfo setAngreDokumentLogiskSlettet(DokumentInfo dokumentInfo) {
		dokumentInfo.setSlettet(false);
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		dokumentInfo.setTittel(fjernSlettemelding(dokumentInfo.getTittel()));
		return dokumentInfo;
	}

	private static String setSlettemelding(String tittel) {
		int minneAllokertForTittel = DokumentInfo.getMaxTitleLength();
		String nyTittel = tittel;

		if (nyTittel.length() + SLETTEMELDING.length() <= minneAllokertForTittel) {
			nyTittel += SLETTEMELDING;
		}
		return nyTittel;
	}

	private static String fjernSlettemelding(String tittel) {
		String nyTittel = tittel;

		if (tittel.endsWith(SLETTEMELDING)) {
			nyTittel = tittel.substring(0, tittel.length() - SLETTEMELDING.length());
		}
		return nyTittel;
	}

}
