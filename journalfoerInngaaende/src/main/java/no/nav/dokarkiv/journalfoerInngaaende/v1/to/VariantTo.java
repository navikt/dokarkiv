package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class VariantTo {
	private String arkivfiltype;
	private String variantformat;
}
