package no.nav.dokarkiv.safintern.views;

import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.safintern.views.FetchPaths.erGyldig;
import static org.assertj.core.api.Assertions.assertThat;

class FetchPathsTest {

	@Test
	void shouldVerifyNumFetchPaths() {
		// Sjekker antallet pga det kan få negative konsekvenser for klienter/tilgangskontroll hvis man fjerner en fetch path
		assertThat(FetchPaths.GYLDIGE_PATHS).hasSize(27);
	}

	@Test
	void shouldReturnTrueWhenGyldig() {
		assertThat(erGyldig("journalpostId")).isTrue();
	}

	@Test
	void shouldReturnFalseWhenNotGyldig() {
		assertThat(erGyldig("journalpostIdz")).isFalse();
	}
}