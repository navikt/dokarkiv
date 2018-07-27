package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Transfer object for TJOARK066 OppdaterJournalpostRequest
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 23.05.2017.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostRequestTo {
	
	private String endringssporing;
	private OppdaterJournalpostTo oppdaterJournalpostTo;
	
}
