package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;

/**
 * Builder for Kryssreferanse.
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

	public KryssreferanseBuilder kryssreferanseId(Long value) { this.kryssreferanseId = value; return this; }
	public KryssreferanseBuilder referanseId(String value) { this.referanseId = value; return this; }
	public KryssreferanseBuilder referanseType(ReferanseTypeCode value) { this.referanseType = value; return this; }
	public KryssreferanseBuilder referanseNr(Long value) { this.referanseNr = value; return this; }

	@Override
	public Kryssreferanse build() {
		Kryssreferanse kryssreferanse = new Kryssreferanse(kryssreferanseId, 1);
		kryssreferanse.setReferanseId(referanseId);
		kryssreferanse.setReferanseType(referanseType);
		kryssreferanse.setReferanseNr(referanseNr);
		return kryssreferanse;
	}
}
