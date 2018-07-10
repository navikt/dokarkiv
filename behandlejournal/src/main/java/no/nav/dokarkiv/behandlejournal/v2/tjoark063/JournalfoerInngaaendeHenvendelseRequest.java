package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Domain request object for the JournalfoerInngaaendeHenvendelseMedHoveddokument service.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class JournalfoerInngaaendeHenvendelseRequest {

	private Journalpost journalpost;

	/**
	 * Default constructor only used for mapping.
	 */
	@SuppressWarnings("unused")
	private JournalfoerInngaaendeHenvendelseRequest() {
	}

	/**
	 * Constructor taking request fields as parameter.
	 *
	 * @param journalpost The Journalpost object in request.
	 */
	public JournalfoerInngaaendeHenvendelseRequest(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	/**
	 * Check that journalpost is set.
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
	 * Getter for the Journalpost property.
	 *
	 * @return the Journalpost object.
	 */
	public Journalpost getJournalpost() {
		return journalpost;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("journalpost", journalpost)
				.toString();
	}

}
