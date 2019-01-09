package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
@AllArgsConstructor
public class TilgangDokumentInfoDto {

	private final String dokumentinfoId;
	private final String dokumentstatus;
	private final String brevkode;
	private final String variantFormat;

}
