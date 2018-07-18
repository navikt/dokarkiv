package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;

import java.util.ArrayList;
import java.util.List;


/**
 * Response object for operation OpprettogFerdigstillJournalpost.
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 16.02.2017
 */
@Data
@Builder
@AllArgsConstructor
public class JournalforInngaaendeForsendelseResponseTo {
	private final Long journalpostId;
	private final Long dokumentInfoIdHoveddokument;
	private final List<DokumentInfoIdVedleggTo> dokumentInfoIdVedleggTo = new ArrayList<>();
}