package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

/**
 * Builder for SkannetInnhold.
 */
@Deprecated // bruk lombok builder istedet
public class SkannetInnholdBuilder extends Builder<SkannetInnhold> {

	private SkannetInnholdBuilder() {
	}

	public static SkannetInnholdBuilder getSkannetInnholdBuilder() {
		return new SkannetInnholdBuilder();
	}

	private Long skannetInnholdId;
	private Integer vedleggNr;
	private String vedleggInnhold;
	private String dokumenttypeId;

	public SkannetInnholdBuilder skannetInnholdId(Long value) { this.skannetInnholdId = value; return this; }
	public SkannetInnholdBuilder vedleggNr(Integer value) { this.vedleggNr = value; return this; }
	public SkannetInnholdBuilder vedleggInnhold(String value) { this.vedleggInnhold = value; return this; }
	public SkannetInnholdBuilder dokumenttypeId(String value) { this.dokumenttypeId = value; return this; }

	@Override
	public SkannetInnhold build() {
		SkannetInnhold skannetInnhold = new SkannetInnhold(skannetInnholdId, 1);
		skannetInnhold.setVedleggNr(vedleggNr);
		skannetInnhold.setVedleggInnhold(vedleggInnhold);
		skannetInnhold.setDokumenttypeid(dokumenttypeId);
		return skannetInnhold;
	}


}
