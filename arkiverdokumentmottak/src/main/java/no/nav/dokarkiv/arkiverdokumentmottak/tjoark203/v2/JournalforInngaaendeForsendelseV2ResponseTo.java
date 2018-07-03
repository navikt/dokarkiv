package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for TJOARK203 JournalførinngåendeForsendelse
 *
 * @author Paul Magne Lunde, Visma Consulting
 */
public class JournalforInngaaendeForsendelseV2ResponseTo {

	private Long journalpostId;
	private Long dokumentInfoIdHoveddokument;
	private List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo = new ArrayList<>();
	private String journalTilstand;

	public JournalforInngaaendeForsendelseV2ResponseTo(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	public JournalforInngaaendeForsendelseV2ResponseTo(Long journalpostId, Long dokumentInfoIdHoveddokument,
													   List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo, String journalTilstand) {
		this.journalpostId = journalpostId;
		this.dokumentInfoIdHoveddokument = dokumentInfoIdHoveddokument;
		this.dokumentInfoIdVedleggTo = dokumentInfoIdVedleggTo;
		this.journalTilstand = journalTilstand;
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

	public String getJournalTilstand() {
		return journalTilstand;
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

	public void setJournalTilstand(String journalTilstand) {
		this.journalTilstand = journalTilstand;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("journalpostId", journalpostId)
				.append("dokumentInfoIdHoveddokument", dokumentInfoIdHoveddokument)
				.append("dokumentInfoIdVedleggListe", dokumentInfoIdVedleggTo)
				.append("journalTilstand", journalTilstand)
				.toString();
	}
}
