package no.nav.dokarkiv.core.consumer.aktoer;

import lombok.Data;

/**
 * Request object for HentAktoerIdForIdent
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Data
public class HentAktoerIdForIdentRequestTo {
	private final String ident;
}
