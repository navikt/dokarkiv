package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.SkannetInnhold;

/**
 * Builder for SkannetInnhold.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
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
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	private String dokumenttypeId;

	public SkannetInnholdBuilder skannetInnholdId(Long value) { this.skannetInnholdId = value; return this; }
	public SkannetInnholdBuilder vedleggNr(Integer value) { this.vedleggNr = value; return this; }
	public SkannetInnholdBuilder vedleggInnhold(String value) { this.vedleggInnhold = value; return this; }
	public SkannetInnholdBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public SkannetInnholdBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }
	public SkannetInnholdBuilder dokumenttypeId(String value) { this.dokumenttypeId = value; return this; }
	
	@Override
	public SkannetInnhold build() {
		SkannetInnhold skannetInnhold = new SkannetInnhold(skannetInnholdId, 1);
		skannetInnhold.setVedleggNr(vedleggNr);
		skannetInnhold.setVedleggInnhold(vedleggInnhold);
		skannetInnhold.setOpprettetKildeNavn(opprettetKildeNavn);
		skannetInnhold.setEndretKildeNavn(endretKildeNavn);
		skannetInnhold.setDokumenttypeId(dokumenttypeId);
		return skannetInnhold;
	}
	

}
