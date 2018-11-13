package no.nav.dokarkiv.core.consumer.aktoer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for HentAktoerIdForIdent
 *
 * @author Ketill Fenne, Visma Consulting.
 */
@Data
public class HentIdentForAktoerIdResponseTo {

	private final String ident;

	public HentIdentForAktoerIdResponseTo(String ident) {
		this.ident = ident;
	}
}
