package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Magnar Brandsdal, Visma Consulting
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArkiverVedleggResponseTo {

	private long journalpostId;
	private long dokumentInfoId;

	public static ArkiverVedleggResponseTo create(long journalpostId, long dokumentInfoId) {
		ArkiverVedleggResponseTo to = new ArkiverVedleggResponseTo();
		to.journalpostId = journalpostId;
		to.dokumentInfoId = dokumentInfoId;
		return to;
	}
}
