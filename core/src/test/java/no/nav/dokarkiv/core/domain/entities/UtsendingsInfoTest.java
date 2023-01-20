package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UtsendingsInfoTest {

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
						"postkasseleverandør")));
	}

	@Test
	void shouldThrowExceptionWhenNavNoVarslingConstructedWithWrongUtsendingskanal() {
		Journalpost journalpost = new Journalpost();
		journalpost.setUtsendingskanal(UtsendingsKanalCode.S);
		assertThrows(IllegalArgumentException.class, () ->
				new UtsendingsInfo(journalpost, new UtsendingsInfo.NavNoVarsling("navno-identifikator-for-mottaker",
						"Hei Bruker! Du har fått en ny melding på nav.no. Hilsen NAV")));
	}
}