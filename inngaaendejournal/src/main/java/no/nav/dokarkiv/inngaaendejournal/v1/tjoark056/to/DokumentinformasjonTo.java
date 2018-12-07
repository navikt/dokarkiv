package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

import java.util.List;
import java.util.stream.Collectors;

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
	private List<DokumentInnholdTo> dokumentInnhold;

	public List<DokumentInnholdTo> getDokumentInnhold() {
		return dokumentInnhold.stream().filter(innhold -> !innhold.getVariantFormat().equals(VariantFormatCode.SLADDET)).collect(Collectors.toList());
	}
}
