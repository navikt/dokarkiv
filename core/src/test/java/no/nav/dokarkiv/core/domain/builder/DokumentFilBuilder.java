package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.DokumentFil;
import no.nav.dokarkiv.core.domain.FilDetaljer;

/**
 * Builder for DokumentFil.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Deprecated // bruk lombok builder istedet
public class DokumentFilBuilder extends Builder<DokumentFil> {

	private DokumentFilBuilder() {
	}
	
	public static DokumentFilBuilder getDokumentFilBuilder() {
		return new DokumentFilBuilder();
	}
	
	private Long id;
	private String filUuid = FilDetaljer.generateUuid();
	private byte[] fil;
	private String opprettetKildeNavn;
	private Long versjon;
	
	public DokumentFilBuilder id(Long value) { this.id = value; return this; }
	public DokumentFilBuilder filUuid(String value) { this.filUuid = value; return this; }
	public DokumentFilBuilder fil(byte[] value) { this.fil = value; return this; }
	public DokumentFilBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public DokumentFilBuilder versjon(Long value) { this.versjon = value; return this; }
	
	@Override
	public DokumentFil build() {
		DokumentFil dokumentFil = new DokumentFil(id, versjon != null ? versjon : 1);
		dokumentFil.setId(id);
		dokumentFil.setFilUuid(filUuid);
		dokumentFil.setFil(fil);
		dokumentFil.setOpprettetKildeNavn(opprettetKildeNavn);
		return dokumentFil;
	}

}
