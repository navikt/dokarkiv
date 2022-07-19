package no.nav.dokarkiv.core.consumers.saf.journalpost;

public class SafJsonJournalpost {

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
