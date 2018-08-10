package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.Builder;
import lombok.Data;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class AktoerTo {
	private String type;
	private String identifikator;
	private String navn;
}
