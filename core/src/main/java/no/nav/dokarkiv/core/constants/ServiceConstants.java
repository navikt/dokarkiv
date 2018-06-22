package no.nav.dokarkiv.core.constants;

/**
 * Contains constants for the service layer.
 *
 * @deprecated Lag en konstantklasse for hver tjeneste i sine respektive moduler ved behov
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Magnus Skuland, Sirius IT
 */
@Deprecated
public final class ServiceConstants {

	/** TJOARK025 servlet param. */
	public static final String HENT_DOKUMENT_SERVLET_PARAM = "docToken";
	
	/** TJOARK202 tilleggsopplysnings-key **/
	public static final String FORSENDELSE_MOTTAK_ID_KEY = "ForsendelseMottakId";

	/** TJOARK100 tilleggsopplysnings-key **/
	public static final String BESTILLINGS_ID_KEY = "bestillingsId";

	/**
	 * Private constructor to avoid instantiation.
	 */
	private ServiceConstants() {
	}


}
