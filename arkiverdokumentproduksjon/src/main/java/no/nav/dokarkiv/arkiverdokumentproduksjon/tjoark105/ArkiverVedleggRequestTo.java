package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

/**
 * Request object for ArkiverDokumentproduksjon.arkiverVedlegg
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public class ArkiverVedleggRequestTo {

	private Long journalpostId;
	private String endretAvNavn;
	private DokumentInfo dokumentInfo;
	private Boolean ferdigstillDokument;

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

	public DokumentInfo getDokumentInfo() {
		return dokumentInfo;
	}

	public void setDokumentInfo(DokumentInfo dokumentInfo) {
		this.dokumentInfo = dokumentInfo;
	}

	public Boolean getFerdigstillDokument() {
		return ferdigstillDokument;
	}

	public void setFerdigstillDokument(Boolean ferdigstillDokument) {
		this.ferdigstillDokument = ferdigstillDokument;
	}
}
