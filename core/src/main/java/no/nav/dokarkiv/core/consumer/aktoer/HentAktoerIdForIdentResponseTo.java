package no.nav.dokarkiv.core.consumer.aktoer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for HentAktoerIdForIdent
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Data
public class HentAktoerIdForIdentResponseTo {

	private final String aktoerId;
	private final List<IdentDetaljerTo> historiskeIdenter;

	public HentAktoerIdForIdentResponseTo(String aktoerId, List<IdentDetaljerTo> historiskeIdenter) {
		this.aktoerId = aktoerId;
		this.historiskeIdenter = new ArrayList<>(historiskeIdenter);
	}
}
