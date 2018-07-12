package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

/**
 * Defines the contract for the ArkiverUstrukturertKrav operation.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public interface ArkiverUstrukturertKrav {

	/**
	 * Persists the Journalpost contained within the request. Sets internal
	 * values based on functional requirements.
	 * 
	 * @param arkiverUstrukturertKravRequest
	 *            Contains the Journalpost that is to be archived (persisted)
	 * @return Contains the journalpostId of the persisted journalpost as a
	 *         String
	 */
	ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(
			ArkiverUstrukturertKravRequest arkiverUstrukturertKravRequest);

}
