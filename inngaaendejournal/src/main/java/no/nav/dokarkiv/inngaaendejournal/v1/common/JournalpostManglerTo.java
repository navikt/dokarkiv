package no.nav.dokarkiv.inngaaendejournal.v1.common;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
public class JournalpostManglerTo {
	@NonNull
	private final JournalfoeringsbehovTo avsenderId;
	@NonNull
	private final JournalfoeringsbehovTo avsenderNavn;
	@NonNull
	private final JournalfoeringsbehovTo arkivSak;
	@NonNull
	private final JournalfoeringsbehovTo innhold;
	@NonNull
	private final JournalfoeringsbehovTo tema;
	@NonNull
	private final JournalfoeringsbehovTo bruker;
	@NonNull
	private final DokumentInformasjonManglerTo hoveddokument;
	private List<DokumentInformasjonManglerTo> vedlegg;
}
