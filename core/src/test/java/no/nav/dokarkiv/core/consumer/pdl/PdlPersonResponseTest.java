package no.nav.dokarkiv.core.consumer.pdl;

import org.junit.jupiter.api.Test;

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

	@Test
	void shouldMapNavnWithNullMellomnavn() {
		PdlPersonResponse.PdlNavn pdlNavn = new PdlPersonResponse.PdlNavn();
		pdlNavn.setFornavn("Bjarne");
		pdlNavn.setMellomnavn(null);
		pdlNavn.setEtternavn("Betjent");

		assertThat(pdlNavn.getFulltNavn()).isEqualTo("Bjarne Betjent");
	}

	@Test
	void shouldMapNavnWithBlankMellomnavn() {
		PdlPersonResponse.PdlNavn pdlNavn = new PdlPersonResponse.PdlNavn();
		pdlNavn.setFornavn("Bjarne");
		pdlNavn.setMellomnavn("");
		pdlNavn.setEtternavn("Betjent");

		assertThat(pdlNavn.getFulltNavn()).isEqualTo("Bjarne Betjent");
	}

	@Test
	void shouldMapNavnWithWhitespaceMellomnavn() {
		PdlPersonResponse.PdlNavn pdlNavn = new PdlPersonResponse.PdlNavn();
		pdlNavn.setFornavn("Bjarne");
		pdlNavn.setMellomnavn(" ");
		pdlNavn.setEtternavn("Betjent");

		assertThat(pdlNavn.getFulltNavn()).isEqualTo("Bjarne Betjent");
	}
}