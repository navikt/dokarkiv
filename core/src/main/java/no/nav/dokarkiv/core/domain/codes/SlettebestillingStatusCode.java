package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.domain.entities.Slettebestilling;

import java.util.function.Predicate;

public enum SlettebestillingStatusCode implements Predicate<Slettebestilling> {
	OPPRETTET,
	FERDIGSTILT,
	AVBRUTT;

	@Override
	public boolean test(Slettebestilling slettebestilling) {
		return slettebestilling.getSlettebestillingStatus() == this;
	}
}
