package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.Kryssreferanse;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;

/**
 * Builder for Kryssreferanse.
 *
 * @author Thao Thanh Nguyen, Visma Sirius
 */
@Deprecated // bruk lombok builder istedet
public class KryssreferanseBuilder extends Builder<Kryssreferanse> {

	private KryssreferanseBuilder() {
	}
	
	public static KryssreferanseBuilder getKryssreferanseBuilder() {
		return new KryssreferanseBuilder();
	}
	
	private Long kryssreferanseId;
	private String referanseId;
	private ReferanseTypeCode referanseType;
	private Long referanseNr;
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	
	public KryssreferanseBuilder kryssreferanseId(Long value) { this.kryssreferanseId = value; return this; }
	public KryssreferanseBuilder referanseId(String value) { this.referanseId = value; return this; }
	public KryssreferanseBuilder referanseType(ReferanseTypeCode value) { this.referanseType = value; return this; }
	public KryssreferanseBuilder referanseNr(Long value) { this.referanseNr = value; return this; }
	public KryssreferanseBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public KryssreferanseBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }

	@Override
	public Kryssreferanse build() {
		Kryssreferanse kryssreferanse = new Kryssreferanse(kryssreferanseId, 1);
		kryssreferanse.setReferanseId(referanseId);
		kryssreferanse.setReferanseType(referanseType);
		kryssreferanse.setReferanseNr(referanseNr);
		kryssreferanse.setOpprettetKildeNavn(opprettetKildeNavn);
		kryssreferanse.setEndretKildeNavn(endretKildeNavn);
		return kryssreferanse;
	}
	
	
}
