package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.Assert;


/**
 * Request object for ArkiverDokumentproduksjon.avbrytJournalpost
 *
 * @author 'Alan Smithee' 
 */
public class AvbrytJournalpostRequestTo {

	private Long journalpostId;
	private String endretAvNavn;
	
	public AvbrytJournalpostRequestTo(Long journalpostId, String endretAvNavn) {
		super();
		this.journalpostId = journalpostId;
		this.endretAvNavn = endretAvNavn;
	}

	public void validate() {
		if (journalpostId == null ||journalpostId == 0) {
			throw new IllegalArgumentException("JournalpostId cannot be empty or missing");
		}
		Assert.hasText(endretAvNavn, "EndretAvNavn cannot be empty or missing");
	}
	
	public Long getJournalpostId() {
		return journalpostId;
	}


	public void setJournalpostId(Long journalpostId) {
		this.journalpostId = journalpostId;
	}


	public String getEndretAvNavn() {
		return endretAvNavn;
	}


	public void setEndretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
	}
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("journalpostId", journalpostId)
		.append("endretAvNavn", endretAvNavn)
		.toString();
	}
	
}
