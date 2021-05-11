package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for DokumentproduksjonInfo.hentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class HentJournalOgDokumentStatusRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;

	public void validate() {
		if (journalpostId == null || journalpostId == 0) {
			throw new InvalidArgumentException("Missing parameter journalpostId");
		}
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("journalpostId", journalpostId)
		.append("dokumentInfoId", dokumentInfoId)
		.toString();
	}
	
}
