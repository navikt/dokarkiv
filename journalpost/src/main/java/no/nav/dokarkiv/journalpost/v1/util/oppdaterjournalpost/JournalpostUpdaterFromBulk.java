package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.repository.UtsendingsInfoRepository;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.DigitalPost;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.EpostVarsel;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.Postadresse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.SmsVarsel;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.Varsel;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;

@Component
@Slf4j
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
			final Long journalpostId = journalpost.getJournalpostId();

			if (utsendingsInfoRepository.existsById(journalpostId)) {
				UtsendingsInfo utsendingsInfo = utsendingsInfoRepository.findById(journalpostId)
						.orElseThrow(() -> new DokarkivFunctionalException("Fant ikke UtsendingsInfo med journalpostId=" + journalpostId));
				switch (utsendingskanal) {
					case S -> utsendingsInfo.setFysiskPostadresse(from(request.getPostadresse()));
					case SDP -> {
						utsendingsInfo.setDigitalPostadresse(from(request.getDigitalpostkasse()));
						utsendingsInfo.setEpostVarsler(epostVarsel(request.getVarsel()));
						utsendingsInfo.setSmsVarsler(smsVarsel(request.getVarsel()));
					}
					case NAV_NO -> {
						var epostVarsler = epostVarsel(request.getVarsel());
						var smsVarsler = smsVarsel(request.getVarsel());
						utsendingsInfo.setEpostVarsler(epostVarsler);
						utsendingsInfo.setSmsVarsler(smsVarsler);
						boolean kunNavNoVarsel = epostVarsler.isEmpty() && smsVarsler.isEmpty();

						if (kunNavNoVarsel) {
							utsendingsInfo.setNavNoVarsling(lagNavNoVarselOgLogg(request.getVarsel(), kunNavNoVarsel, journalpostId));
						}
					}
					// default: no action - eventuelle feil er håndtert i valideringssteget
				}
			} else {
				switch (utsendingskanal) {
					case S ->
							utsendingsInfoRepository.persist(new UtsendingsInfo(journalpost, from(request.getPostadresse())));
					case SDP ->
							utsendingsInfoRepository.persist(new UtsendingsInfo(journalpost, from(request.getDigitalpostkasse()), epostVarsel(request.getVarsel()), smsVarsel(request.getVarsel())));
					case NAV_NO -> {
						var epostVarsler = epostVarsel(request.getVarsel());
						var smsVarsler = smsVarsel(request.getVarsel());
						boolean kunNavNoVarsel = epostVarsler.isEmpty() && smsVarsler.isEmpty();
						utsendingsInfoRepository.persist(new UtsendingsInfo(journalpost, lagNavNoVarselOgLogg(request.getVarsel(), kunNavNoVarsel, journalpostId), epostVarsler, smsVarsler));
					}
					// default: no action - eventuelle feil er håndtert i valideringssteget
				}
			}
		}

		if (request.getSettStatusEkspedert()) {
			tracker.setEndretFlagg(true);
			tracker.add(JOURNALPOST_JOURNALSTATUS, journalpost.getJournalstatus().name(), JournalStatusCode.E.name());
			journalpost.setJournalstatus(JournalStatusCode.E);
			journalpost.setEkspedertDato(request.getEkspedertDato());
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

	private static UtsendingsInfo.NavNoVarsling lagNavNoVarselOgLogg(Varsel varsel, boolean kunNavNoVarsel, long journalpostId) {
		if (varsel.getDigitalkontaktinformasjon() == null && varsel.getVarseltekst() == null) {
			return null;
		}
		if (kunNavNoVarsel) {
			log.info("bulkOppdaterDistribusjonsinfo har mottatt NavNoVarsling for journalpostId={} i gammelt format men ikke epost- eller sms-varsel", journalpostId);
		}
		return new UtsendingsInfo.NavNoVarsling(varsel.getDigitalkontaktinformasjon(), varsel.getVarseltekst());
	}

	private static UtsendingsInfo.EpostVarsler epostVarsel(Varsel varsel) {
		if (varsel == null || varsel.getEpostvarsel() == null) {
			return new UtsendingsInfo.EpostVarsler(emptyList());
		}
		return new UtsendingsInfo.EpostVarsler(varsel.getEpostvarsel().stream().map(EpostVarsel::toInternal).toList());
	}

	private static UtsendingsInfo.SmsVarsler smsVarsel(Varsel varsel) {
		if (varsel == null || varsel.getSmsvarsel() == null) {
			return new UtsendingsInfo.SmsVarsler(emptyList());
		}
		return new UtsendingsInfo.SmsVarsler(varsel.getSmsvarsel().stream().map(SmsVarsel::toInternal).toList());
	}
}
