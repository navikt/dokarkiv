package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * RequestTo object for ArkiverDokumentproduksjon.fjernFerdigstiltDokumentRequest
 * 
 * @author Stig Strøm
 */
public class FjernFerdigstiltDokumentRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;
	private String endretAvNavn;

	public FjernFerdigstiltDokumentRequestTo(Long journalpostId, Long dokumentInfoId, String endretAvNavn) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.endretAvNavn = endretAvNavn;
	}

	public Long getJournalpostId() {
		return journalpostId;
	}
	
	public void setDokumentInfoId(Long dokumentInfoId) {
		this.dokumentInfoId = dokumentInfoId;
	}

	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}
	
	public String getEndretAvNavn() {
		return endretAvNavn;
	}

	public void setEndretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
	}

	public void setJournalpostId(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("journalpostId", journalpostId)
		.append("dokumentInfoId", dokumentInfoId)
		.append("endretAvNavn", endretAvNavn)
		.toString();
	}
}
