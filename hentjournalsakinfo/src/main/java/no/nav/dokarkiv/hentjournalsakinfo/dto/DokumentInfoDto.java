package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DokumentInfoDto {
	Long dokumentInfoId;
	@JsonIgnore
	String tilknyttetSom;
	@JsonIgnore
	Long jpRelasjonId;
	String dokumentstatus;
	Date datoFerdigstilt;
	String brevkode;
	String dokumenttypeId;
	List<VariantDto> varianter;
	String tittel;
	String skjerming;
	Long origJournalpostId;
	boolean kassert;
	List<LogiskVedleggDto> logiske;
	String kategori;
	boolean sensitivt;
}
