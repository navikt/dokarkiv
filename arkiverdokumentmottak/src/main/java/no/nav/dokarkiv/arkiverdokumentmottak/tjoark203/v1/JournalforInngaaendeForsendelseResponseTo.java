package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;

import java.util.ArrayList;
import java.util.List;


/**
 * Response object for operation OpprettogFerdigstillJournalpost.
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 16.02.2017
 */
@Data
@AllArgsConstructor
public class JournalforInngaaendeForsendelseResponseTo {

	private Long journalpostId;
	private Long dokumentInfoIdHoveddokument;
	private List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo = new ArrayList<>();

	public JournalforInngaaendeForsendelseResponseTo(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

}