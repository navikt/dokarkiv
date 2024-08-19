package no.nav.dokarkiv.core.domain.entities;

import com.blazebit.persistence.CTE;

import javax.persistence.Entity;
import javax.persistence.Id;

@CTE
@Entity
public class JournalpostIdSubqueryCte {

	private Long journalpostId;

	@Id
	public Long getJournalpostId() { return journalpostId; }
	public void setJournalpostId(Long journalpostId) { this.journalpostId = journalpostId; }
}
