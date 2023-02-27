package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UtsendingsInfoTest {

	private static final UtsendingsInfo.EpostVarsler epostVarsler = new UtsendingsInfo.EpostVarsler(List.of(new UtsendingsInfo.EpostVarsel("tittel", "tekst", "homer@epos.gr", "2023-02-27T12:30:00.000")));
	private static final UtsendingsInfo.SmsVarsler smsVarsler = new UtsendingsInfo.SmsVarsler(List.of(new UtsendingsInfo.SmsVarsel("tekst", "+4700000000", "2023-02-27T12:30:00.000")));

	@Test
	void shouldThrowExceptionWhenFysiskPostadresseConstructedWithWrongUtsendingskanal() {
		Journalpost journalpost = new Journalpost();
		journalpost.setUtsendingskanal(UtsendingsKanalCode.NAV_NO);
		assertThrows(IllegalArgumentException.class, () ->
				new UtsendingsInfo(journalpost, new UtsendingsInfo.FysiskPostadresse("varslegate 1",
						null, null, "0101", "Oslo", "NO")));
	}

	@Test
	void shouldThrowExceptionWhenDigitalPostadresseConstructedWithWrongUtsendingskanal() {
		Journalpost journalpost = new Journalpost();
		journalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		assertThrows(IllegalArgumentException.class, () ->
				new UtsendingsInfo(journalpost, new UtsendingsInfo.DigitalPostadresse("postmottaker#1323",
						"postkasseleverandør"), epostVarsler, smsVarsler));
	}

	@Test
	void shouldThrowExceptionWhenNavNoVarslingConstructedWithWrongUtsendingskanal() {
		Journalpost journalpost = new Journalpost();
		journalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		assertThrows(IllegalArgumentException.class, () ->
				new UtsendingsInfo(journalpost, new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker",
						null), epostVarsler, smsVarsler));
	}
}