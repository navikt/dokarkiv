package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.Builder;
import lombok.Data;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class BrukerTo {
	private String type;
	private String identifikator;
}
