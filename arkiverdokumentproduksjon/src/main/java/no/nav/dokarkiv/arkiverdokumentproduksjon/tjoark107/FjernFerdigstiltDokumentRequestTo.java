package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import lombok.Data;


/**
 * RequestTo object for ArkiverDokumentproduksjon.fjernFerdigstiltDokumentRequest
 *
 * @author Stig Strøm
 */
@Data
public class FjernFerdigstiltDokumentRequestTo {
	private final Long journalpostId;
	private final Long dokumentInfoId;
	private final String endretAvNavn;
}
