package no.nav.service.dok.joark.nsb.to;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * RequestTo object for ArkiverDokumentproduksjon.avbrytVedlegg
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class AvbrytVedleggRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;
	private String endretAvNavn;

	public AvbrytVedleggRequestTo(Long journalpostId, Long dokumentInfoId, String endretAvNavn) {
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
		return ReflectionToStringBuilder.toString(this);
	}
}
