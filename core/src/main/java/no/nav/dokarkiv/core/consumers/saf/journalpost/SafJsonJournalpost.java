package no.nav.dokarkiv.core.consumers.saf.journalpost;

import java.io.Serializable;

public class SafJsonJournalpost implements Serializable {

	static final long serialVersionUID = 1234567L;

	private DataJournalpost data;

	public DataJournalpost getData() {
		return data;
	}

	public void setData(DataJournalpost data) {
		this.data = data;
	}

	public SafJournalpostTo getJournalpost() {
		return data.getJournalpost();
	}
}
