package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.Assert.assertThrows;

class CommonValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = {"", "@#%^", "åååå-her-er-det-noen-ulovlige-tegn?", "-- ; drop table users ; --",
			"__EN_STRENG_MED_201_TEGN___aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
	void shouldThrowInputValideringFeiletInvalidEksternReferanseId(String eksternReferanseId) {
		assertThrows(InputValideringFeiletException.class,
				() -> CommonValidator.validateEksternReferanseId(eksternReferanseId));
	}
}