package no.nav.dokarkiv.core.consumer.pdl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PdlPersonResponseTest {

	@Test
	void shouldMapFulltnavn() {
		PdlPersonResponse.PdlNavn pdlNavn = new PdlPersonResponse.PdlNavn();
		pdlNavn.setFornavn("Leonora");
		pdlNavn.setMellomnavn("Dorothea");
		pdlNavn.setEtternavn("Dahl");

		assertThat(pdlNavn.getFulltNavn()).isEqualTo("Leonora Dorothea Dahl");
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", " "})
	void shouldMapNavnWithMellomnavn(String mellomnavn) {
		PdlPersonResponse.PdlNavn pdlNavn = new PdlPersonResponse.PdlNavn();
		pdlNavn.setFornavn("Bjarne");
		pdlNavn.setMellomnavn(mellomnavn);
		pdlNavn.setEtternavn("Betjent");

		assertThat(pdlNavn.getFulltNavn()).isEqualTo("Bjarne Betjent");
	}
}