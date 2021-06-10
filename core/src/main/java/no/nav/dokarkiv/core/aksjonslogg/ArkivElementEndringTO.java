package no.nav.dokarkiv.core.aksjonslogg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArkivElementEndringTO {
	private String arkivElement;
	private String fraVerdi;
	private String tilVerdi;
}
