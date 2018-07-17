package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for TJOARK203 JournalførinngåendeForsendelse
 *
 * @author Paul Magne Lunde, Visma Consulting
 */
@Data
@AllArgsConstructor
public class JournalforInngaaendeForsendelseV2ResponseTo {

	private final Long journalpostId;
	private Long dokumentInfoIdHoveddokument;
	private List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo = new ArrayList<>();
	private String journalTilstand;

	public JournalforInngaaendeForsendelseV2ResponseTo(Long journalpostId) {
		this.journalpostId = journalpostId;
	}


}
