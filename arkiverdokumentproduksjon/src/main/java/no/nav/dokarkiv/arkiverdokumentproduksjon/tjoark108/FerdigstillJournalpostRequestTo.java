package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * RequestTo object for ArkiverDokumentproduksjon.ferdigstillJournalpost
 *
 * @author Stig Strøm
 */
public class FerdigstillJournalpostRequestTo {

	private Long journalpostId;
	private String endretAvNavn;
	private UtsendingsKanalCode utsendingskanal;


	public FerdigstillJournalpostRequestTo(Long journalpostId, String endretAvNavn, UtsendingsKanalCode utsendingskanal) {
		this.journalpostId = journalpostId;
		this.endretAvNavn = endretAvNavn;
		this.utsendingskanal = utsendingskanal;
	}

	public Long getJournalpostId() {
		return journalpostId;
	}

	public String getEndretAvNavn() {
		return endretAvNavn;
	}

	public UtsendingsKanalCode getUtsendingskanal() {
		return utsendingskanal;
	}

	public void setEndretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
	}

	public void setJournalpostId(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	public void setUtsendingskanal(UtsendingsKanalCode utsendingskanal) {
		this.utsendingskanal = utsendingskanal;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("journalpostId", journalpostId)
				.append("endretAvNavn", endretAvNavn)
				.append("utsendingskanal", utsendingskanal)
				.toString();
	}
}
