package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentmalInfo;

/**
 * Builder for DokumentFil.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Deprecated // bruk lombok builder istedet
public class DokumentmalInfoBuilder extends Builder<DokumentmalInfo> {

	private DokumentmalInfoBuilder() {
	}
	
	public static DokumentmalInfoBuilder getDokumentmalInfoBuilder() {
		return new DokumentmalInfoBuilder();
	}
	
	private String brevkode;
	private String brevgruppe;
	private String tittel;
	private Boolean redigerbart;
	private Boolean organInternt;
	private Boolean sensitivt;
	private DokumentKategoriCode dokumentKategori;
	private JournalpostTypeCode journalpostType;
	
	public DokumentmalInfoBuilder brevkode(String value) { this.brevkode = value; return this; }
	public DokumentmalInfoBuilder brevgruppe(String value) { this.brevgruppe = value; return this; }
	public DokumentmalInfoBuilder tittel(String value) { this.tittel = value; return this; }
	public DokumentmalInfoBuilder redigerbart(Boolean value) { this.redigerbart = value; return this; }
	public DokumentmalInfoBuilder organInternt(Boolean value) { this.organInternt = value; return this; }
	public DokumentmalInfoBuilder sensitivt(Boolean value) { this.sensitivt = value; return this; }
	public DokumentmalInfoBuilder dokumentKategori(DokumentKategoriCode value) { this.dokumentKategori = value; return this; }
	public DokumentmalInfoBuilder journalpostType(JournalpostTypeCode value) { this.journalpostType = value; return this; }
	
	@Override
	public DokumentmalInfo build() {
		DokumentmalInfo dokumentmalInfo = new DokumentmalInfo();
		dokumentmalInfo.setBrevkode(brevkode);
		dokumentmalInfo.setBrevgruppe(brevgruppe);
		dokumentmalInfo.setTittel(tittel);
		dokumentmalInfo.setRedigerbart(redigerbart);
		dokumentmalInfo.setOrganInternt(organInternt);
		dokumentmalInfo.setSensitivt(sensitivt);
		dokumentmalInfo.setDokumentKategori(dokumentKategori);
		dokumentmalInfo.setJournalpostType(journalpostType);
		return dokumentmalInfo;
	}

}
