package no.nav.dokarkiv.core.domain.validator;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.dokarkiv.core.domain.validator.FoedselsnummerValidator.isValidPid;
import static org.assertj.core.api.Assertions.assertThat;

class FoedselsnummerValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = {"01117400200", "011174 00200", "27857798800"})
	void shouldValidateFnr(String fnr) {
		assertThat(isValidPid(fnr)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"58088349006", "580883 49006"})
	void shouldValidateDnr(String dnr) {
		assertThat(isValidPid(dnr)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"01117400001", "011174 00000"})
	void shouldNotValidateFnrSpecialCircumstanceIfFlagIsNotSet(String fnr) {
		assertThat(isValidPid(fnr, false)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"01117400001", "011174 00000"})
	void shouldValidateFnrSpecialCircumstanceIfFlagIsSet(String fnr) {
		assertThat(isValidPid(fnr, true)).isTrue();
	}

}