package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * Response object for operation OpprettogFerdigstillJournalpost.
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 16.02.2017
 */
public class JournalforInngaaendeForsendelseResponseTo {

	private Long journalpostId;
	private Long dokumentInfoIdHoveddokument;
	private List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo = new ArrayList<>();


	public JournalforInngaaendeForsendelseResponseTo(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	public JournalforInngaaendeForsendelseResponseTo(Long journalpostId, Long dokumentInfoIdHoveddokument, List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo) {
		this.journalpostId = journalpostId;
		this.dokumentInfoIdHoveddokument = dokumentInfoIdHoveddokument;
		this.dokumentInfoIdVedleggTo = dokumentInfoIdVedleggTo;
	}

	public Long getJournalpostId() {
		return journalpostId;
	}

	public Long getDokumentInfoIdHoveddokument() {
		return dokumentInfoIdHoveddokument;
	}

	public List<DokumentInfoIdVedleggTo> getDokumentInfoIdVedleggTo() {
		return dokumentInfoIdVedleggTo;
	}

	public void setJournalpostId(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	public void setDokumentInfoIdHoveddokument(Long dokumentInfoIdHoveddokument) {
		this.dokumentInfoIdHoveddokument = dokumentInfoIdHoveddokument;
	}

	public void setDokumentInfoIdVedleggTo(List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo) {
		this.dokumentInfoIdVedleggTo = dokumentInfoIdVedleggTo;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("journalpostId", journalpostId)
				.append("dokumentInfoIdHoveddokument", dokumentInfoIdHoveddokument)
				.append("dokumentInfoIdVedleggListe", dokumentInfoIdVedleggTo)
				.toString();
	}
}