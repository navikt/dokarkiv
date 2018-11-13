package no.nav.dokarkiv.core.consumer.aktoer;

import lombok.Data;

/**
 * Request object for HentIdentForAktoerId
 *
 * @author Ketill Fenne, Visma Consulting.
 */
@Data
public class HentIdentForAktoerIdRequestTo {
	private final String aktoerId;
}
