package no.nav.dokarkiv.innsynjournal.v2;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intermediate object to handle Journalpost and DokumentInfo innsyn
 *
 * @author Torgeir Cook, Visma Consulting
 */
public class InnsynJournalpostTo {

	private Journalpost journalpost;
	private AvsenderMottaker avsenderMottaker = AvsenderMottaker.JA;
	private Map<Long, DokumentInnsyn> dokumentInnsyn = new HashMap<>();

	public InnsynJournalpostTo(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	public static List<InnsynJournalpostTo> innsynJournalposts(List<Journalpost> journalposts) {
		ArrayList<InnsynJournalpostTo> innsynJournalposts = new ArrayList<>();
		for (Journalpost journalpost : journalposts) {
			innsynJournalposts.add(new InnsynJournalpostTo(journalpost));
		}
		return innsynJournalposts;
	}

	public Journalpost getJournalpost() {
		return journalpost;
	}

	public Map<Long, DokumentInnsyn> getDokumentInnsyn() {
		return dokumentInnsyn;
	}

	public void putDokumentInnsyn(DokumentInnsyn innsyn, Long dokumentInfoId) {
		this.dokumentInnsyn.put(dokumentInfoId, innsyn);
	}

	public AvsenderMottaker getAvsenderMottaker() {
		return avsenderMottaker;
	}

	public void setAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
		this.avsenderMottaker = avsenderMottaker;
	}

	public enum DokumentInnsyn {
		JA,
		NEI,
		KAN_IKKE_AVGJOERES;
	}

	public enum AvsenderMottaker {
		JA,
		NEI,
		KAN_IKKE_AVGJOERES;
	}

}
