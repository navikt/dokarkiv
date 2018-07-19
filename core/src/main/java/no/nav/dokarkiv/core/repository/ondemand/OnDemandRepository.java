package no.nav.dokarkiv.core.repository.ondemand;

import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;

/**
 * Interface defining methods for accessing OnDemand
 * 
 * @author Carl-Henrik Wolf Lund
 * @author Stian Landsnes, Sirius IT
 */
public interface OnDemandRepository {

	/**
	 * Retrieve a document from OnDemand
	 * 
	 * @param onDemandId
	 *            The onDemandId.
	 * @param onDemandInstans
	 *            Enum code telling from which onDemand instance the document is
	 *            to be retrieved.
	 * @return A bytearray.
	 */
	byte[] getDocument(String onDemandId, OnDemandInstansCode onDemandInstans);

}
