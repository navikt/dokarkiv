package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
@AllArgsConstructor
public class TilgangDokumentInfoDto {

	private final String dokumentinfoId;
	private final DokumentStatusCode dokumentstatus;
	private final String brevkode;
	private final VariantFormatCode variantFormat;

}
