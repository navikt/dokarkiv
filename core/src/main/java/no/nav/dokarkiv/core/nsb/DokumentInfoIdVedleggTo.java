package no.nav.dokarkiv.core.nsb;

/**
 * Support class for DokumentInfoIdVedleggListe for use in {@link no.nav.service.dok.joark.nsb.to.JournalforInngaaendeForsendelseResponseTo}
 * when mapping to response.
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 21.02.2017
 */
public class DokumentInfoIdVedleggTo {

	private long dokumentInfoId;
	private String dokumentTypeId;

	public DokumentInfoIdVedleggTo() {
		/* USED FOR INITIALIZATION */
	}

	public DokumentInfoIdVedleggTo(long dokumentInfoId, String dokumentTypeId) {
		this.dokumentInfoId = dokumentInfoId;
		this.dokumentTypeId = dokumentTypeId;
	}


	public long getDokumentInfoId() {
		return this.dokumentInfoId;
	}

	public void setDokumentInfoId(long value) {
		this.dokumentInfoId = value;
	}

	public String getDokumentTypeId() {
		return this.dokumentTypeId;
	}

	public void setDokumentTypeId(String value) {
		this.dokumentTypeId = value;
	}
}
