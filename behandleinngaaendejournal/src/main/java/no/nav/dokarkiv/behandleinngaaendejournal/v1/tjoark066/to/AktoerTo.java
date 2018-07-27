package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;

/**
 * Transfer object for TJOARK066 Journalpost.Bruker
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 29.05.2017.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AktoerTo {
	
	private String aktoerId;
	private BrukerTypeCode brukerTypeCode;
}