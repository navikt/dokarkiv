package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Transfer object for TJOARK066 Journalpost.AvsendMottaker
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 29.05.2017.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvsenderTo {
	private String avsenderId;
	private String avsenderNavn;
}
