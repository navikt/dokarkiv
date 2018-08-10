package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.Builder;
import lombok.Data;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class LogiskVedleggTo {
	private String logiskVedleggId;
	private String logiskVedleggTittel;
}
