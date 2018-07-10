package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder for BidragMellomlagring.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class BidragMellomlagringBuilder extends Builder<BidragMellomlagring> {

	private BidragMellomlagringBuilder() {
	}
	
	public static BidragMellomlagringBuilder getBidragMellomlagringBuilder() {
		return new BidragMellomlagringBuilder();
	}
	
	private Long bidragMellomlagringId;
	private String avsenderFnr;
	private Date mottattDato;
	private BidragMellomlagringStatus status;
	private Set<BidragMellomlagringDokument> bidragMellomlagringDokuments = new HashSet<BidragMellomlagringDokument>();
	
	public BidragMellomlagringBuilder bidragMellomlagringId(Long value) { this.bidragMellomlagringId = value; return this; }
	public BidragMellomlagringBuilder avsenderFnr(String value) { this.avsenderFnr = value; return this; }
	public BidragMellomlagringBuilder mottattDato(Date value) { this.mottattDato = value; return this; }
	public BidragMellomlagringBuilder status(BidragMellomlagringStatus value) { this.status = value; return this; }
	public BidragMellomlagringBuilder bidragMellomlagringDokuments(BidragMellomlagringDokument... value) { 
		this.bidragMellomlagringDokuments.addAll(Arrays.asList(value)); return this; }
	
	@Override
	public BidragMellomlagring build() {
		BidragMellomlagring bidragMellomlagring = new BidragMellomlagring(bidragMellomlagringId, 1);
		bidragMellomlagring.setAvsenderFnr(avsenderFnr);
		bidragMellomlagring.setMottattDato(mottattDato);
		bidragMellomlagring.setStatus(status);
		for (BidragMellomlagringDokument bidragMellomlagringDokument : bidragMellomlagringDokuments) {
			bidragMellomlagring.addBidragMellomlagringDokument(bidragMellomlagringDokument);
		}
		return bidragMellomlagring;
	}

}
