package no.nav.dokarkiv.internal.dokvaktmester;

import no.nav.dokarkiv.core.api.Fagsaksystem;
import no.nav.dokarkiv.core.api.Sakstype;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EndreFerdigstiltJournalpostValidatorTest {

	private static final String VALID_BEGRUNNELSE = "gyldig-nokkel";
	private static final String VALID_BRUKER_ID = "11111111111";
	private static final String VALID_TEMA = "FOR";

	private final EndreFerdigstiltJournalpostValidator validator = new EndreFerdigstiltJournalpostValidator();

	@Test
	void shouldPassWhenTemaIsSet() {
		var request = new EndreFerdigstiltJournalpostRequest(null, null, VALID_TEMA, VALID_BEGRUNNELSE);
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenAllFieldsAreBlank() {
		var request = new EndreFerdigstiltJournalpostRequest(null, null, null, VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("En av brukerId, sak og tema må være satt");
	}

	@Test
	void shouldPassWhenTemaValid() {
		var request = new EndreFerdigstiltJournalpostRequest(VALID_BRUKER_ID, null, VALID_TEMA, VALID_BEGRUNNELSE);
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenTemaInvalid() {
		var request = new EndreFerdigstiltJournalpostRequest(VALID_BRUKER_ID, null, "hei", VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("tema matcher ikke pattern");
	}

	@Test
	void shouldPassWhenBrukerIdIsNumeric() {
		var request = new EndreFerdigstiltJournalpostRequest(VALID_BRUKER_ID, null, null, VALID_BEGRUNNELSE);
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenBrukerIdIsNotNumeric() {
		var request = new EndreFerdigstiltJournalpostRequest("1234abc4567", null, null, VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("brukerId er ikke numerisk");
	}

	@Test
	void shouldFailWhenBrukerIdHasWrongLength() {
		var request = new EndreFerdigstiltJournalpostRequest("1234567890", null, null, VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("brukerId må ha lengde 11");
	}

	@Test
	void shouldPassWhenFagsakHasFagsakIdAndFagsaksystem() {
		var sak = new EndreSak(Sakstype.FAGSAK, "FS-123", Fagsaksystem.FS38);
		var request = new EndreFerdigstiltJournalpostRequest(null, sak, null, VALID_BEGRUNNELSE);
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenFagsakMissingFagsakId() {
		var sak = new EndreSak(Sakstype.FAGSAK, null, Fagsaksystem.FS38);
		var request = new EndreFerdigstiltJournalpostRequest(null, sak, null, VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("sak.fagsakId og sak.fagsaksystem må være satt hvis sak.sakstype er FAGSAK");
	}

	@Test
	void shouldPassWhenGenerellSakHasNoFagsakIdOrFagsaksystem() {
		var sak = new EndreSak(Sakstype.GENERELL_SAK, null, null);
		var request = new EndreFerdigstiltJournalpostRequest(null, sak, null, VALID_BEGRUNNELSE);
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenGenerellSakHasFagsakId() {
		var sak = new EndreSak(Sakstype.GENERELL_SAK, "FS-123", null);
		var request = new EndreFerdigstiltJournalpostRequest(null, sak, null, VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("sak.fagsakId og sak.fagsaksystem burde ikke være satt hvis sak.sakstype er GENERELL_SAK");
	}

	@Test
	void shouldFailWhenSakstypeIsArkivsak() {
		var sak = new EndreSak(Sakstype.ARKIVSAK, null, null);
		var request = new EndreFerdigstiltJournalpostRequest(null, sak, null, VALID_BEGRUNNELSE);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("sak.sakstype FAGSAK støttes ikke");
	}

	@Test
	void shouldPassWhenBegrunnelseNokkelIsSet() {
		var request = new EndreFerdigstiltJournalpostRequest(null, null, VALID_TEMA, "gyldig-nokkel");
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenBegrunnelseNokkelIsBlank() {
		var request = new EndreFerdigstiltJournalpostRequest(null, null, VALID_TEMA, "");
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("begrunnelseNokkel må være satt");
	}

	@Test
	void shouldPassWhenBegrunnelseNokkelIsExactly40Chars() {
		var nokkel = "a".repeat(40);
		var request = new EndreFerdigstiltJournalpostRequest(null, null, VALID_TEMA, nokkel);
		assertDoesNotThrow(() -> validator.validate(request));
	}

	@Test
	void shouldFailWhenBegrunnelseNokkelExceeds40Chars() {
		var nokkel = "a".repeat(41);
		var request = new EndreFerdigstiltJournalpostRequest(null, null, VALID_TEMA, nokkel);
		var ex = assertThrows(InputValideringFeiletException.class, () -> validator.validate(request));
		assertThat(ex.getMessage()).contains("begrunnelseNokkel må være kortere enn 40 tegn");
	}
}
