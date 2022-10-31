package no.nav.dokarkiv.journal.v3.tjoark058;


import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journalposttyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Tema;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Soekefilter;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertXMLGregorianCalendarToDate;

@Component
public class HentKjerneJournalpostListeRequestMapper {

	protected static final long DEFAULT_RESULTAT_SET_STOERRELSE = 50;
	protected static final int DEFAULT_RESULTAT_NR = 0;

	public HentKjerneJournalpostListeRequestTo map(HentKjerneJournalpostListeRequest request,
												   List<ArkivSak> filteredArkivSakListe) {

		Date journalFom = null;
		Date journalTom = null;
		JournalpostTypeCode journalpostType = null;
		List<FagomradeCode> temaer = new LinkedList<>();
		Soekefilter soekefilter = request.getSoekefilter();

		if (soekefilter != null) {
			journalFom = convertXMLGregorianCalendarToDate(soekefilter.getJournalFom());
			journalTom = convertXMLGregorianCalendarToDate(soekefilter.getJournalTom());

			Journalposttyper journalposttype = soekefilter.getJournalposttype();

			journalpostType = journalposttype == null ? null :
					(journalposttype.getValue() == null ? null : JournalpostTypeCode.valueOf(journalposttype.getValue()));

			for (Tema tema : soekefilter.getTema()) {
				temaer.add(FagomradeCode.valueOf(tema.getValue()));
			}
		}
		return HentKjerneJournalpostListeRequestTo.builder()
				.saksListe(mapSaksListe(filteredArkivSakListe))
				.journalFom(journalFom)
				.journalTom(journalTom)
				.tema(temaer)
				.journalpostType(journalpostType)
				.resultatSettNr(request.getResultatSettNr() == null ? DEFAULT_RESULTAT_NR : request.getResultatSettNr())
				.resultatSettStoerrelse(request.getResultatSettStoerrelse() == null ? DEFAULT_RESULTAT_SET_STOERRELSE : request.getResultatSettStoerrelse())
				.build();
	}

	private List<SakFagsystem> mapSaksListe(List<ArkivSak> arkivSakListe) {
		List<SakFagsystem> sakListe = new ArrayList<>();
		for (ArkivSak arkivSak : arkivSakListe) {
			SakFagsystem sakFagsystem = new SakFagsystem();
			sakFagsystem.setSakId(arkivSak.getArkivSakId());
			sakFagsystem.setFagsystem(FagsystemCode.valueOf(arkivSak.getArkivSakSystem()));
			sakListe.add(sakFagsystem);
		}
		return sakListe;
	}

}
