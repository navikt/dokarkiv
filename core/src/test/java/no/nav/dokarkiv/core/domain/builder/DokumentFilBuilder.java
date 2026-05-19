package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

/**
 * Builder for DokumentFil.
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

	public DokumentFilBuilder id(Long value) { this.id = value; return this; }
	public DokumentFilBuilder filUuid(String value) { this.filUuid = value; return this; }
	public DokumentFilBuilder fil(byte[] value) { this.fil = value; return this; }

	@Override
	public DokumentFil build() {
		DokumentFil dokumentFil = new DokumentFil(id, 1);
		dokumentFil.setId(id);
		dokumentFil.setFilUuid(filUuid);
		dokumentFil.setFil(fil);
		return dokumentFil;
	}

}
