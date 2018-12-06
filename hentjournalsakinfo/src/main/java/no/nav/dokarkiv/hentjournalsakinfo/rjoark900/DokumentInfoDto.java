package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentInfoDto {
	private final Long dokumentInfoId;
	@JsonIgnore
	private final String tilknyttetSom;
	private final DokumentStatusCode dokumentstatus;
	private final String brevkode;
	private final VariantFormatCode variantFormat = VariantFormatCode.ARKIV;
	private final String tittel;
	private final List<LogiskVedleggDto> logiske;
}
