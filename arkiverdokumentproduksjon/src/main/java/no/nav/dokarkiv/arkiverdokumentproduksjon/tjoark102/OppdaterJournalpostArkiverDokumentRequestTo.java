package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Request object for the operation OppdaterJournalpostArkiverDokument
 *
 * @author Torgeir Cook
 */

public class OppdaterJournalpostArkiverDokumentRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;
	private UtsendingsKanalCode utsendingskanal;
	private String endretAvNavn;
	private Date datoDokument;
	private Set<FilDetaljer> fildetaljerSet;
	private boolean ferdigstillJournalpost;

	public OppdaterJournalpostArkiverDokumentRequestTo() {
		fildetaljerSet = new HashSet<>();
	}

	public Long getJournalpostId() {
		return journalpostId;
	}

	public void setJournalpostId(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}

	public void setDokumentInfoId(Long dokumentInfoId) {
		this.dokumentInfoId = dokumentInfoId;
	}

	public UtsendingsKanalCode getUtsendingskanal() {
		return utsendingskanal;
	}

	public void setUtsendingskanal(UtsendingsKanalCode utsendingskanal) {
		this.utsendingskanal = utsendingskanal;
	}

	public String getEndretAvNavn() {
		return endretAvNavn;
	}

	public void setEndretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
	}

	public Date getDatoDokument() {
		return datoDokument != null ? (Date) datoDokument.clone() : null;
	}

	public void setDatoDokument(Date datoDokument) {
		this.datoDokument = datoDokument != null ? new Date(datoDokument.getTime()) : null;
	}

	public Set<FilDetaljer> getFildetaljer() {
		return fildetaljerSet;
	}

	public boolean isFerdigstillJournalpost() {
		return ferdigstillJournalpost;
	}

	public void setFerdigstillJournalpost(boolean ferdigstillJournalpost) {
		this.ferdigstillJournalpost = ferdigstillJournalpost;
	}

	public void addFilDetaljer(FilDetaljer filDetaljer) {
		String fileSize = String.valueOf(filDetaljer.getFileContent().length);
		filDetaljer.setFilstorrelse(fileSize);
		fildetaljerSet.add(filDetaljer);
	}
}
