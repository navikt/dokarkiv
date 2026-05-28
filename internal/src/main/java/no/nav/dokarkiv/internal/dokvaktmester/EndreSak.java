package no.nav.dokarkiv.internal.dokvaktmester;

import no.nav.dokarkiv.core.api.Fagsaksystem;
import no.nav.dokarkiv.core.api.Sakstype;

public record EndreSak(
		Sakstype sakstype,
		String fagsakId,
		Fagsaksystem fagsaksystem
) {

}
