package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder for {@link DokumentInfo}.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Deprecated // bruk lombok builder istedet
public class DokumentInfoBuilder extends Builder<DokumentInfo> {

	private DokumentInfoBuilder() {
	}

	public static DokumentInfoBuilder getDokumentInfoBuilder() {
		return new DokumentInfoBuilder();
	}

	private Long dokumentInfoId;
	private String brevkode;
	private String brevgruppe;
	private Journalpost originalJournalpost;
	private String konvertertFraSystem;
	private Boolean sensitivt;
	private String endretAvNavn;
	private DokumentKategoriCode kategori;
	private DokumentStatusCode dokumentstatus;
	private Date dokumentFerdigDato;
	private String tittel;
	private String brukeroppgittTittel;
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	private String dokumenttypeId;
	private Map<String, String> tilleggsopplysninger = new HashMap<>();
	private List<SkannetInnhold> skannetInnholdList = new ArrayList<>();
	private Set<FilDetaljer> filDetaljerList = new HashSet<>();
	private ChangeStamp changeStamp;

	public DokumentInfoBuilder dokumentInfoId(Long value) { this.dokumentInfoId = value; return this; }
	public DokumentInfoBuilder brevkode(String value) { this.brevkode = value; return this; }
	public DokumentInfoBuilder brevgruppe(String value) { this.brevgruppe = value; return this; }
	public DokumentInfoBuilder originalJournalpost(Journalpost value) { this.originalJournalpost = value; return this; }
	public DokumentInfoBuilder konvertertFraSystem(String value) { this.konvertertFraSystem = value; return this; }
	public DokumentInfoBuilder sensitivt(Boolean value) { this.sensitivt = value; return this; }
	public DokumentInfoBuilder endretAvNavn(String value) { this.endretAvNavn = value; return this; }
	public DokumentInfoBuilder kategori(DokumentKategoriCode value) { this.kategori = value; return this; }
	public DokumentInfoBuilder dokumentstatus(DokumentStatusCode value) { this.dokumentstatus = value ; return this; }
	public DokumentInfoBuilder dokumentFerdigDato(Date value) { this.dokumentFerdigDato = value; return this; }
	public DokumentInfoBuilder tittel(String value) { this.tittel = value; return this; }
	public DokumentInfoBuilder brukeroppgittTittel(String value) { this.brukeroppgittTittel = value; return this; }
	public DokumentInfoBuilder dokumenttypeId(String value) { this.dokumenttypeId = value; return this; }
	public DokumentInfoBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public DokumentInfoBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }
	public DokumentInfoBuilder skannetInnhold(SkannetInnhold... value) {
		this.skannetInnholdList = Arrays.asList(value); return this; }
	public DokumentInfoBuilder filDetaljerList(FilDetaljer... value) {
		this.filDetaljerList.addAll(Arrays.asList(value)); return this; }
	public DokumentInfoBuilder tilleggsopplysninger(Map<String, String> value) {
		this.tilleggsopplysninger = value; return this; }
	public DokumentInfoBuilder changeStamp(ChangeStamp changeStamp) { this.changeStamp = changeStamp; return this; }

	@Override
	public DokumentInfo build() {
		DokumentInfo dokumentInfo = new DokumentInfo(dokumentInfoId, 1);
		dokumentInfo.setBrevkode(brevkode);
		dokumentInfo.setBrevgruppe(brevgruppe);
		dokumentInfo.setOriginalJournalpost(originalJournalpost);
		dokumentInfo.setKonvertertFraSystem(konvertertFraSystem);
		dokumentInfo.setSensitivt(sensitivt);
		dokumentInfo.setEndretAvNavn(endretAvNavn);
		dokumentInfo.setKategori(kategori);
		dokumentInfo.setDokumentstatus(dokumentstatus);
		dokumentInfo.setDokumentFerdigDato(dokumentFerdigDato);
		dokumentInfo.setTittel(StringUtils.isEmpty(brukeroppgittTittel) ? tittel: brukeroppgittTittel);
		dokumentInfo.setOpprettetKildeNavn(opprettetKildeNavn);
		dokumentInfo.setEndretKildeNavn(endretKildeNavn);
		dokumentInfo.setTilleggsopplysninger(tilleggsopplysninger);
		dokumentInfo.setDokumenttypeId(dokumenttypeId);
		for (SkannetInnhold skannetInnhold : skannetInnholdList) {
			dokumentInfo.addSkannetInnhold(skannetInnhold);
		}
		for (FilDetaljer filDetaljer : filDetaljerList) {
			dokumentInfo.addFilDetaljer(filDetaljer);
		}
		dokumentInfo.setChangeStamp(changeStamp);

		return dokumentInfo;
	}

}
