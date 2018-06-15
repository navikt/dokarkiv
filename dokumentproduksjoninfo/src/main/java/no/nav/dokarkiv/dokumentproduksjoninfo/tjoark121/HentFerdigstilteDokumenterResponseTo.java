package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import java.util.Arrays;

/**
 * To response-object for HentFerdigstilteDokumenterService(TJOARK121) 
 * 
 * @author Stig Strøm
 *
 */
public class HentFerdigstilteDokumenterResponseTo {
	
	private Long dokumentInfoId;
	private byte[] fil;
	private String tittel;
	
	public HentFerdigstilteDokumenterResponseTo(Long dokumentInfoId, byte[] fil, String tittel) {
		this.dokumentInfoId = dokumentInfoId;
		this.fil = Arrays.copyOf(fil, fil.length);
		this.tittel = tittel;
	}

	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}
	public void setDokumentInfoId(Long dokumentInfoId) {
		this.dokumentInfoId = dokumentInfoId;
	}
	
	public byte[] getFil() {
		return Arrays.copyOf(fil, fil.length);
	}
	public void setFil(byte[] fil) {
		this.fil = Arrays.copyOf(fil, fil.length);
	}
	
	public String getTittel() {
		return tittel;
	}
	public void setTittel(String tittel) {
		this.tittel = tittel;
	}
	
	@Override
	public String toString() {
		return "HentFerdigstilteDokumenterResponseTo [dokumentInfoId=" + dokumentInfoId + ", fil=" + Arrays.toString(fil)
				+ ", tittel=" + tittel + "]";
	}	
}
