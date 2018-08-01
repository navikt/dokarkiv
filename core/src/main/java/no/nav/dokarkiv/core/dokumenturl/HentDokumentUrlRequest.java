package no.nav.dokarkiv.core.dokumenturl;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for the service HentDokumentUrl.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public class HentDokumentUrlRequest {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***94578515L;

	private Long journalpostId;
	private String filUuid;
	private Long timeToLiveMinutes;
	private Boolean ikkeBrukSSL;

	/**
	 * Constructor for mapping
	 */
	@SuppressWarnings("unused")
	private HentDokumentUrlRequest() {
	}

	/**
	 * Constructs a HentDokumentUrlRequest.
	 * 
	 * @param journalpostId The journalpostId.
	 * @param filUuid the filUuid.
	 */
	public HentDokumentUrlRequest(Long journalpostId, String filUuid) {
		this.journalpostId = journalpostId;
		this.filUuid = filUuid;
	}
	
	/**
	 * Constructs a new HentDokumentUrlRequest.
	 *
	 * @param journalpostId The journalpostId.
	 * @param filUuid The filUUid.
	 * @param timeToLiveMinutes How long the URL should be valid.
	 * @param ikkeBrukSSL Used to override environment config of URL
	 */
	public HentDokumentUrlRequest(Long journalpostId, String filUuid, Long timeToLiveMinutes, Boolean ikkeBrukSSL) {
		this.journalpostId = journalpostId;
		this.filUuid = filUuid;
		this.timeToLiveMinutes = timeToLiveMinutes;
		this.ikkeBrukSSL = ikkeBrukSSL;
	}

	
	/**
	 * Validate that the request parameters are set.
	 */
	public void validate() {
		//journalpostId will never be null, only 0L if it's not set.
		throwExceptionIfParameterIsNull(filUuid, "FilUuid");
		if (filUuid.length() == 0){
			throw new InvalidArgumentException("Parameter filUuid is empty");
		}
	}

	private void throwExceptionIfParameterIsNull(Object parameter, String parameterName) {
		if (parameter == null) {
			throw new InvalidArgumentException("Missing parameter", parameterName, parameter);
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
	 * Getter for the timeToLiveMinutes property.
	 *
	 * @return the timeToLiveMinutes
	 */
	public Long getTimeToLiveMinutes() {
		return timeToLiveMinutes;
	}

	/**
	 * Getter for the ikkeBrukSSL property.
	 *
	 * @return the ikkeBrukSSL
	 */
	public Boolean getIkkeBrukSSL() {
		return ikkeBrukSSL;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("journalpostId", journalpostId)
			.append("filUuid", filUuid)
			.append("timeToLiveMinutes", timeToLiveMinutes)
			.append("ikkeBrukSSL", ikkeBrukSSL)
			.toString();
	}

}
