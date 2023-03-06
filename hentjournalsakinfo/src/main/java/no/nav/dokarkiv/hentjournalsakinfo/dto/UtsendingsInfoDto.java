package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.Value;

@Value
public class UtsendingsInfoDto {
	FysiskPostadresse fysiskPostadresse;
	DigitalPostadresse digitalPostadresse;
	NavNoVarsling navNoVarsling;
	String epostVarsel;
	String smsVarsel;

	@Value
	public static class FysiskPostadresse {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postnummer;
		String poststed;
		String landkode;
	}

	@Value
	public static class DigitalPostadresse {
		String digitalpostkasseAdresse;
		String postkasseLeverandor;
	}

	@Value
	public static class NavNoVarsling {
		String varselSendtTil;
		String varseltekst;
	}
}
