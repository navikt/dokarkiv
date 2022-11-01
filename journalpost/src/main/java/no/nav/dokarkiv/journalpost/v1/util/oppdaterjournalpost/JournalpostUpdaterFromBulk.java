package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.DigitalPost;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.NavNoVarsel;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.Postadresse;
import org.slf4j.MDC;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;

public class JournalpostUpdaterFromBulk {

	public static ChangeTracker updateFields(Journalpost journalpost, JournalpostWithDistribusjonsinfo request) {
		ChangeTracker tracker = new ChangeTracker();

		if (request.getUtsendingsKanal() != null) {
			UtsendingsKanalCode utsendingskanal = UtsendingsKanalCode.valueOf(request.getUtsendingsKanal());
			journalpost.setUtsendingskanal(utsendingskanal);

			switch (utsendingskanal) {
				case S -> journalpost.setUtsendingsInfo(from(request.getPostadresse()));
				case SDP -> journalpost.setUtsendingsInfo(from(request.getDigitalpostkasse()));
				case NAV_NO -> journalpost.setUtsendingsInfo(from(request.getVarsel()));
				// default: no action - eventuelle feil er håndtert i valideringssteget
			}
		}

		if (request.getSettStatusEkspedert()) {
			journalpost.setJournalstatus(JournalStatusCode.E);
			journalpost.setEkspedertDato(request.getEkspedertDato());
			tracker.setEndretFlagg(true);
			tracker.add(JOURNALPOST_JOURNALSTATUS, journalpost.getJournalstatus().name(), JournalStatusCode.E.name());
		}

		if (tracker.isEndretFlagg()) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_NAME));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
		return tracker;
	}

	private static UtsendingsInfo.FysiskPostadresse from(Postadresse postadresse) {
		return new UtsendingsInfo.FysiskPostadresse(
				postadresse.getAdresselinje1(),
				postadresse.getAdresselinje2(),
				postadresse.getAdresselinje3(),
				postadresse.getPostnummer(),
				postadresse.getPoststed(),
				postadresse.getLandkode()
		);
	}

	private static UtsendingsInfo.DigitalPostadresse from(DigitalPost digitalpost) {
		return new UtsendingsInfo.DigitalPostadresse(digitalpost.getDigitalpostkasseadresse(), digitalpost.getDigitalpostkasseleverandor());
	}

	private static UtsendingsInfo.NavNoVarsling from(NavNoVarsel navNoVarsel){
		return new UtsendingsInfo.NavNoVarsling(navNoVarsel.getDigitalkontaktinformasjon(), navNoVarsel.getVarseltekst());
	}
}
