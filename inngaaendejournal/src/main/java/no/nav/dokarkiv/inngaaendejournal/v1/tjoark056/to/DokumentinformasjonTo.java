package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
public class DokumentinformasjonTo {
	private DokumentKategoriCode dokumentkategori;
	private String dokumenttypeId;
	@NonNull
	private final Long dokumentId;
	private DokumenttilstandTo dokumenttilstand;
	@NonNull
	private final List<DokumentInnholdTo> dokumentInnhold;
}
