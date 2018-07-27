package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;

import java.util.List;

/**
 * Transfer object for TJOARK066 Journalpost
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 23.05.2017.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OppdaterJournalpostTo {
	
	private String journalpostId;
	private AvsenderTo avsenderTo;
	private String innhold;
	private ArkivSakTo arkivSak;
	private FagomradeCode tema;
	private AktoerTo aktoerTo;
	private DokumentInformasjonTo hoveddokument;
	private List<DokumentInformasjonTo> vedlegg;
	
}