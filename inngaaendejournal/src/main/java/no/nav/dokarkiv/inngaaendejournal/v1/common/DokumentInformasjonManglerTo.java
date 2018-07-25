package no.nav.dokarkiv.inngaaendejournal.v1.common;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
public class DokumentInformasjonManglerTo {
	@NonNull
	private final JournalfoeringsbehovTo dokumentKategori;
	@NonNull
	private final Long dokumentId;
	@NonNull
	private final JournalfoeringsbehovTo tittel;
}
