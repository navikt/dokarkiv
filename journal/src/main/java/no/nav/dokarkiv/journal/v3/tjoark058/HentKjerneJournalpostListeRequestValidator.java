package no.nav.dokarkiv.journal.v3.tjoark058;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertXMLGregorianCalendarToDate;

import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Soekefilter;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

/**
 * Validates required input for TJOARK058 HentKjerneJournalpostListe.
 *
 * @author Stig Strøm, Acando
 */
public class HentKjerneJournalpostListeRequestValidator {
	
	private int predefinertAntallSaker;
	
	public HentKjerneJournalpostListeRequestValidator(int predefinertAntallSaker) {
		super();
		this.predefinertAntallSaker = predefinertAntallSaker;
	}
	
	public void validate(HentKjerneJournalpostListeRequest request) {
		Assert.notNull(request, "Input request er null");
		Assert.notEmpty(request.getArkivSakListe(), "ArkivSakListe er tom eller null");
		
		List<ArkivSak> arkivSakListe = request.getArkivSakListe();
		for (ArkivSak arkivSak : arkivSakListe) {
			Assert.hasLength(arkivSak.getArkivSakId(), "ArkivSakId er tom eller null. " + arkivSak.toString());
			Assert.hasLength(arkivSak.getArkivSakSystem(),
					"ArkivSakSystem er tom eller null. arkivSakId=" + arkivSak.getArkivSakId());
		}
		
		if (arkivSakListe.size() > predefinertAntallSaker) {
			throw new IllegalArgumentException("Saksliste må begrenses. Maks tillatte saker er " + predefinertAntallSaker + ", men var " + arkivSakListe.size());
		}
		
		validateSoekefilter(request);
	}
	
	private void validateSoekefilter(HentKjerneJournalpostListeRequest request) {
		Soekefilter soekefilter = request.getSoekefilter();
		if (soekefilter != null) {
			XMLGregorianCalendar journalFom = soekefilter.getJournalFom();
			if (journalFom != null) {
				DateTime fom = new DateTime(convertXMLGregorianCalendarToDate(journalFom));
				checkFomIsAfterNow(fom, journalFom);
				
				if (soekefilter.getJournalTom() != null) {
					DateTime tom = new DateTime(convertXMLGregorianCalendarToDate(soekefilter.getJournalTom()));
					checkFomIsAfterTom(fom, tom);
				}
			}
		}
	}
	
	private void checkFomIsAfterNow(DateTime fom, XMLGregorianCalendar requestFom) {
		if (fom.isAfterNow()) {
			throw new IllegalArgumentException("Ugyldig datointervall. JournalFom er etter dagens dato. journalFom=" + requestFom);
		}
	}
	
	private void checkFomIsAfterTom(DateTime fom, DateTime tom) {
		if (fom.isAfter(tom)) {
			throw new IllegalArgumentException("Ugyldig datointervall. JournalFom er etter journalTom. journalFom=" + fom + ", journalTom=" + tom);
		}
	}
}