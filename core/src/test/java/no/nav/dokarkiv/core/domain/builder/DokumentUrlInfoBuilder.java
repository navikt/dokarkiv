package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.Date;

/**
 * Builder for DokumentUrlInfo
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
@Deprecated // bruk lombok builder istedet
public class DokumentUrlInfoBuilder extends Builder<DokumentUrlInfo> {

	private DokumentUrlInfoBuilder() {		
	}
	
	public static DokumentUrlInfoBuilder getDokumentUrlInfoBuilder() {
		return new DokumentUrlInfoBuilder();
	}
	
	private Long dokumentUrlInfoId;
	private String docToken;
	private Journalpost journalpost;
	private String filUuid;
	private Date tidspunkt;
	private Long timeToLiveMinutes;
	
	public DokumentUrlInfoBuilder dokumentUrlInfoId(Long value) { this.dokumentUrlInfoId = value; return this; }
	public DokumentUrlInfoBuilder docToken(String value) { this.docToken = value; return this; }
	public DokumentUrlInfoBuilder journalpost(Journalpost value) { this.journalpost = value; return this; }
	public DokumentUrlInfoBuilder filUuid(String value) { this.filUuid = value; return this; }
	public DokumentUrlInfoBuilder tidspunkt(Date value) { this.tidspunkt = value; return this; }
	public DokumentUrlInfoBuilder timeToLiveMinutes(Long value) { this.timeToLiveMinutes = value; return this; }
	
	@Override
	public DokumentUrlInfo build() {
		DokumentUrlInfo dokumentUrlInfo = new DokumentUrlInfo(dokumentUrlInfoId, 1);
		dokumentUrlInfo.setDocToken(docToken);
		dokumentUrlInfo.setJournalpost(journalpost);
		dokumentUrlInfo.setFilUuid(filUuid);
		dokumentUrlInfo.setTidspunkt(tidspunkt);
		dokumentUrlInfo.setTimeToLiveMinutes(timeToLiveMinutes);
		return dokumentUrlInfo;
	}

}
