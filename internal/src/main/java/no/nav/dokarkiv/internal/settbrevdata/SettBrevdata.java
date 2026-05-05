package no.nav.dokarkiv.internal.settbrevdata;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

public record SettBrevdata(String filUuid, int filstoerrelse, Handling handling) {

	public static SettBrevdata from(FilDetaljer filDetaljer, Handling handling) {
		return new SettBrevdata(filDetaljer.getFilUuid(), Integer.parseInt(filDetaljer.getFilstorrelse()), handling);
	}

	public enum Handling {
		OPPRETTET_DOKUMENT,
		OPPDATERT_DOKUMENT,
		INGEN
	}
}
