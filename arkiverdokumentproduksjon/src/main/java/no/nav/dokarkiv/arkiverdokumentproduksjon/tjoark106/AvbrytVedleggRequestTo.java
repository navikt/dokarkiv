package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import lombok.Data;

/**
 * RequestTo object for ArkiverDokumentproduksjon.avbrytVedlegg
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
@Data
public class AvbrytVedleggRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final String endretAvNavn;
}
