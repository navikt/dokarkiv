package no.nav.dokarkiv.hentdokument.dokument;

import static org.apache.logging.log4j.util.Strings.isBlank;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for the service HentDokument.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Lamisi Gurah Blackman, Accenture
 */
public class HentDokumentRequest {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = -9182145370779656751L;

	private Long journalpostId;
	private String filUuid;
	private String docToken;

	/**
	 * Constructor for mapping
	 */
	@SuppressWarnings("unused")
	private HentDokumentRequest() {
	}

	/**
	 * Constructs a HentDokumentRequest.
	 *
	 * @param journalpostId The journalpostId.
	 * @param filUuid       The filUuid of the file to get.
	 */
	public HentDokumentRequest(Long journalpostId, String filUuid) {
		this.journalpostId = journalpostId;
		this.filUuid = filUuid;
	}

	/**
	 * Constructs a HentDokumentRequest.
	 *
	 * @param journalpostId The journalpostId.
	 * @param filUuid       The filUuid of the file to get.
	 * @param docToken      The docToken
	 */
	public HentDokumentRequest(Long journalpostId, String filUuid, String docToken) {
		this.journalpostId = journalpostId;
		this.filUuid = filUuid;
		this.docToken = docToken;
	}

	/**
	 * Validate that the request parameters are set.
	 */
	public void validate() {
		if (journalpostId == null) {
			throw new InvalidArgumentException("Missing parameter", "JournalpostId", journalpostId);
		}
		if (isBlank(filUuid)) {
			throw new InvalidArgumentException("Missing parameter", "filUuid", filUuid);
		}
	}

	/**
	 * Getter for the journalpostId property.
	 *
	 * @return the journalpostId
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}

	/**
	 * Getter for the filUuid property.
	 *
	 * @return the filUuid
	 */
	public String getFilUuid() {
		return filUuid;
	}

	/**
	 * Getter for the docToken property.
	 *
	 * @return the docToken
	 */
	public String getDocToken() {
		return docToken;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("journalpostId", journalpostId).toString();
	}

}
