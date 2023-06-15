package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

@Value
@Builder
@AllArgsConstructor
public class TilgangDokumentInfoDto {

	String dokumentinfoId;
	DokumentStatusCode dokumentstatus;
	String brevkode;
	DokumentKategoriCode kategori;
	Boolean kassert;
	SkjermingTypeCode skjerming;
	TilgangVariantDto variant;

}
