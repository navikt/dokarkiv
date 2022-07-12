package no.nav.dokarkiv.core.consumers.saf.journalpost;

import java.io.Serializable;

public class DataJournalpost implements Serializable {

	static final long serialVersionUID = 1234566L;
	private SafJournalpostTo journalpost;

	public SafJournalpostTo getJournalpost() {
		return journalpost;
	}

	public void setJournalpost(SafJournalpostTo journalpost) {
		this.journalpost = journalpost;
	}
}