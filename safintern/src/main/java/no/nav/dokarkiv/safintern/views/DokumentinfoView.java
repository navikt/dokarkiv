package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

import java.util.Date;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"dokumentInfoId"})
@EntityView(JournalpostDokumentInfoRelasjon.class)
public interface DokumentinfoView {
	@JsonIgnore
	@IdMapping("journalpostDokumentInfoRelasjonId")
	Long getId();

	@Mapping("dokumentInfo.dokumentInfoId")
	Long getDokumentInfoId();

	@Mapping("tilknyttetJournalpostSom")
	TilknyttetJournalpostSomCode getTilknyttetSom();

	@Mapping("skjermingType")
	SkjermingTypeCode getSkjerming();

	@Mapping("dokumentInfo.brevkode")
	String getBrevkode();

	@Mapping("dokumentInfo.sensitivt")
	Boolean getSensitivt();

	@Mapping("dokumentInfo.kategori")
	DokumentKategoriCode getKategori();

	@Mapping("dokumentInfo.dokumentstatus")
	DokumentStatusCode getStatus();

	@Mapping("dokumentInfo.dokumentFerdigDato")
	Date getFerdigDato();

	@Mapping("dokumentInfo.tittel")
	String getTittel();

	@Mapping("dokumentInfo.originalJournalpost.journalpostId")
	Long getOriginalJournalpostId();

	@Mapping("dokumentInfo.dokumenttypeId")
	String getDokumenttypeId();

	@Mapping("dokumentInfo.kassert")
	Boolean getKassert();

	@Mapping("dokumentInfo.fildetaljerListe")
	@CollectionMapping
	List<FildetaljerView> getFildetaljer();

	@Mapping("dokumentInfo.skannetInnholdListe")
	@CollectionMapping
	Set<LogiskVedleggView> getLogiskVedlegg();
}
