package no.nav.dokarkiv.core.consumer.aktoer;

import lombok.Data;

import java.util.Date;

/**
 * Object representing ident details.
 * Used by {@link HentAktoerIdForIdentResponseTo}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Data
public class IdentDetaljerTo {
	private final String fnr;
	private final Date datoFom;
}
