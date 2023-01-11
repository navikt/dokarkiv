package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.repository.UtsendingsInfoRepository;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.DigitalPost;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.NavNoVarsel;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.Postadresse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;

@Component
public class JournalpostUpdaterFromBulk {

	private static final String UNKNOWN_ALPHA3_LANDKODE = "???";
	private static final String UNKNOWN_ALPHA2_LANDKODE = "??";

	private final UtsendingsInfoRepository utsendingsInfoRepository;

	public JournalpostUpdaterFromBulk(UtsendingsInfoRepository utsendingsInfoRepository) {
		this.utsendingsInfoRepository = utsendingsInfoRepository;
	}

	public ChangeTracker updateFields(Journalpost journalpost, JournalpostWithDistribusjonsinfo request) {
		ChangeTracker tracker = new ChangeTracker();

		if (request.getUtsendingsKanal() != null) {
			UtsendingsKanalCode utsendingskanal = UtsendingsKanalCode.valueOf(request.getUtsendingsKanal());
			journalpost.setUtsendingskanal(utsendingskanal);

			if(utsendingsInfoRepository.existsById(journalpost.getJournalpostId())) {
				UtsendingsInfo utsendingsInfo = utsendingsInfoRepository.findById(journalpost.getJournalpostId())
						.orElseThrow(() -> new DokarkivFunctionalException("Fant ikke UtsendingsInfo med journalpostId=" + journalpost.getJournalpostId()));
				switch (utsendingskanal) {
					case S -> utsendingsInfo.setFysiskPostadresse(from(request.getPostadresse()));
					case SDP -> utsendingsInfo.setDigitalPostadresse(from(request.getDigitalpostkasse()));
					case NAV_NO -> utsendingsInfo.setNavNoVarsling(from(request.getVarsel()));
					// default: no action - eventuelle feil er håndtert i valideringssteget
				}
			} else {
				switch (utsendingskanal) {
					case S -> utsendingsInfoRepository.persist(new UtsendingsInfo(journalpost, from(request.getPostadresse())));
					case SDP -> utsendingsInfoRepository.persist(new UtsendingsInfo(journalpost, from(request.getDigitalpostkasse())));
					case NAV_NO -> utsendingsInfoRepository.persist(new UtsendingsInfo(journalpost, from(request.getVarsel())));
					// default: no action - eventuelle feil er håndtert i valideringssteget
				}
			}
		}

		if (request.getSettStatusEkspedert()) {
			journalpost.setJournalstatus(JournalStatusCode.E);
			journalpost.setEkspedertDato(request.getEkspedertDato());
			tracker.setEndretFlagg(true);
			tracker.add(JOURNALPOST_JOURNALSTATUS, journalpost.getJournalstatus().name(), JournalStatusCode.E.name());
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_NAME));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
		return tracker;
	}

	private static UtsendingsInfo.FysiskPostadresse from(Postadresse postadresse) {

		return postadresse == null ? null : new UtsendingsInfo.FysiskPostadresse(
				postadresse.getAdresselinje1(),
				postadresse.getAdresselinje2(),
				postadresse.getAdresselinje3(),
				postadresse.getPostnummer(),
				postadresse.getPoststed(),
				UNKNOWN_ALPHA3_LANDKODE.equals(postadresse.getLandkode()) ? UNKNOWN_ALPHA2_LANDKODE : postadresse.getLandkode()
		);
	}

	private static UtsendingsInfo.DigitalPostadresse from(DigitalPost digitalpost) {
		return new UtsendingsInfo.DigitalPostadresse(digitalpost.getDigitalpostkasseadresse(), digitalpost.getDigitalpostkasseleverandor());
	}

	private static UtsendingsInfo.NavNoVarsling from(NavNoVarsel navNoVarsel) {
		return new UtsendingsInfo.NavNoVarsling(navNoVarsel.getDigitalkontaktinformasjon(), navNoVarsel.getVarseltekst());
	}
}
