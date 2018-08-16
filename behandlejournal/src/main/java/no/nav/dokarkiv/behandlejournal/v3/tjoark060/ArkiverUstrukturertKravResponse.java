package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import lombok.Data;

/**
 * Response object for the ArkiverUstrukturertKrav service.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
@Data
public class ArkiverUstrukturertKravResponse {
	private final Long journalpostId;
	private final Long dokumentId;
}
