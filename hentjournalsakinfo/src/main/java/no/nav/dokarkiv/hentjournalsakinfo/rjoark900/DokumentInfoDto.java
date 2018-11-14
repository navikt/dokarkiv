package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentInfoDto {
	private final String dokumentInfoId;
	private final DokumentStatusCode dokumentstatus;
	private final String brevkode;
	private final VariantFormatCode variantFormat;
	private final String tittel;

	public DokumentInfoDto(DokumentInfo dokumentInfo) {
		this.dokumentInfoId = dokumentInfo.getDokumentInfoId().toString();
		this.dokumentstatus = dokumentInfo.getDokumentstatus();
		this.brevkode = dokumentInfo.getBrevkode();
		this.tittel = dokumentInfo.getTittel();
		this.variantFormat = VariantFormatCode.ARKIV;
	}
}
