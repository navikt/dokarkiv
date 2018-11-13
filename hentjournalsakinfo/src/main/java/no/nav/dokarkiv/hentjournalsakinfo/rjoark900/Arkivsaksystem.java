package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Arkivsaksystem {
	GSAK(FagsystemCode.FS22),
	PSAK(FagsystemCode.PEN);

	private final FagsystemCode joarkMapping;

	Arkivsaksystem(FagsystemCode joarkMapping) {
		this.joarkMapping = joarkMapping;
	}

	public FagsystemCode getJoarkMapping() {
		return joarkMapping;
	}
}
