package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;

/**
 * Builder for BidragMellomlagringDokument.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class BidragMellomlagringDokumentBuilder extends Builder<BidragMellomlagringDokument>{

	private BidragMellomlagringDokumentBuilder() {
	}
	
	public static BidragMellomlagringDokumentBuilder getBidragMellomlagringDokumentBuilder() {
		return new BidragMellomlagringDokumentBuilder();
	}
	
	private Long bidragMellomlagringDokumentId;
	private BidragMellomlagringDokumentType dokumentType;
	private byte[] dokument;
	private ChangeStamp changeStamp;

	public BidragMellomlagringDokumentBuilder bidragMellomlagringDokumentId(Long value) {
		this.bidragMellomlagringDokumentId = value; return this; }
	public BidragMellomlagringDokumentBuilder dokumentType(BidragMellomlagringDokumentType value) { 
		this.dokumentType = value; return this; }
	public BidragMellomlagringDokumentBuilder dokument(byte[] value) {	this.dokument = value; return this; }
	public BidragMellomlagringDokumentBuilder changeStamp(ChangeStamp value) {	this.changeStamp = value; return this; }
	
	@Override
	public BidragMellomlagringDokument build() {
		BidragMellomlagringDokument bidragMellomlagringDokument = new BidragMellomlagringDokument(
				bidragMellomlagringDokumentId, 1);
		bidragMellomlagringDokument.setDokumentType(dokumentType);
		bidragMellomlagringDokument.setDokument(dokument);
		bidragMellomlagringDokument.setChangeStamp(changeStamp);
		return bidragMellomlagringDokument;
	}

}
