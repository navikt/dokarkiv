package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

import java.util.Date;
import java.util.List;

@Value
public class DokumentInfoDto {
	Long dokumentInfoId;
	@JsonIgnore
	String tilknyttetSom;
	@JsonIgnore
	Long jpRelasjonId;
	DokumentStatusCode dokumentstatus;
	Date datoFerdigstilt;
	String brevkode;
	String dokumenttypeId;
	List<VariantDto> varianter;
	String tittel;
	SkjermingTypeCode skjerming;
	Long origJournalpostId;
	boolean kassert;
	List<LogiskVedleggDto> logiske;
	DokumentKategoriCode kategori;
	boolean sensitivt;
}
