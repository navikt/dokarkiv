package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AktoerTo {
	String arkivfiltype;
	String variantformat;
}
