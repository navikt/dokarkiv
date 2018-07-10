package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The request object for the JournalfoerNotatHenvendelse service.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseRequest {

	private Journalpost journalpost;

	/**
	 * Needed for mapping
	 */
	@SuppressWarnings("unused")
	private JournalfoerNotatHenvendelseRequest() {
	}

	/**
	 * Constructs a new OppdaterMidlertidigJournalpostRequest.
	 *
	 * @param journalpost The journalpost.
	 */
	public JournalfoerNotatHenvendelseRequest(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	/**
	 * Validate that journalpost with id is set.
	 */
	public void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Missing parameter in request: Journalpost");
		}

		if (journalpost.findHoveddokumentDokumentInfoRelasjon() == null) {
			throw new ApplicationException("Missing parameter in request: Hoveddokument");
		}
	}

	/**
	 * Getter for the journalpost property.
	 *
	 * @return the journalpost
	 */
	public Journalpost getJournalpost() {
		return journalpost;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("journalpost", journalpost).toString();
	}

}
