package no.nav.dokarkiv.core.repository.journalpostliste;

import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Params used for searching and filtering for journalposts.
 * 
 * saksListe - Filtering journalpost based on saksId and Fagsystem
 * tidligstInnsynDato - Filtering out journalposts where changeStamp.createdDate and journalDato< than specified date
 * tillattInnsynStatus - Filtering on {@link JournalStatusCode}
 * visFeilRegistrert - Filtering on saksrelasjon.feilregistrert
 * skjulFagomraade - Filtering on Fagområde
 * journalFom - Filtering on Journalpost changeStamp.createdDate>= specified date
 * journalTom - Filtering on Journalpost changeStamp.createdDate<= specified date
 * journalpostTypeCode - Filtering on {@link JournalpostTypeCode}
 * eagerFetchDokInfo - eager fetch optimization on associated entities
 * Created by Hans Petter Simonsen - Visma Consulting
 */
@Data
public class HentMinJPListeParameters {
	private List<SakFagsystem> saksListe = new ArrayList<>();
	private Date tidligstInnsynDato;
	private List<JournalStatusCode> tillattInnsynStatus = new ArrayList<>();
	private boolean visFeilRegistrert = false;
	private List<FagomradeCode> skjulFagomraade = new ArrayList<>();
	private List<FagomradeCode> fagomraade = new ArrayList<>();
	private Date journalFom;
	private Date journalTom;
	private JournalpostTypeCode journalpostTypeCode;
	private boolean eagerFetchDokInfo = true;
	private long maxResults;
	private int pageNr;
	private boolean includeBegrensetJournalpost = false;

	public void addFagsystemSak(String sakid, FagsystemCode fagsystem) {
		saksListe.add(new SakFagsystem(fagsystem, sakid));
	}
	public void addFagsystemSak(SakFagsystem fagsystemSak) {
		saksListe.add(fagsystemSak);
	}
}
