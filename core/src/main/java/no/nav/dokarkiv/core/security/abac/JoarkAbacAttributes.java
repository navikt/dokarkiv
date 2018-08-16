package no.nav.dokarkiv.core.security.abac;

/**
 * Joark specific ABAC config parameters
 *
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
public class JoarkAbacAttributes {

    /* Samme som applikasjonsnavn i Fasit */
    public static final String PEP_ID = "joark";

    public static final String READ_ACTION = "read";
	public static final String CREATE_ACTION = "create";
    public static final String UPDATE_ACTION = "update";
	public static final String ARKIV = "arkiv";
}
