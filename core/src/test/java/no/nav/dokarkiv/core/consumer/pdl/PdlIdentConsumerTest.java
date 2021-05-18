package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.sts.StsRestConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class PdlIdentConsumerTest {

	@InjectMocks
	protected PdlIdentConsumer pdlIdentConsumer = new PdlIdentConsumer(
			"http://pdl-dummy",
			new RestTemplateBuilder(),
			Mockito.mock(StsRestConsumer.class)
	);


	@Test
	public void ShouldValidateFnrWith11Numbers() {
		String validatedIdent = pdlIdentConsumer.validateFolkeregisterIdent("11111111111");
		assertEquals("11111111111", validatedIdent);
	}

	@Test
	public void ShouldValidateAktoerIdWith13Numbers() {
		String validatedIdent = pdlIdentConsumer.validateFolkeregisterIdent("1111111111111");
		assertEquals("1111111111111", validatedIdent);
	}

	@Test
	public void ShouldValidateAktoerIdWith13Numbers2() {
		String validatedIdent = pdlIdentConsumer.validateFolkeregisterIdent("    11111111111    ");
		assertEquals("11111111111", validatedIdent);
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsCorrectLengthWithCharactersThatIsNotNumeric() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent("1test11test"));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNull() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent(null));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsEmpty() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent(""));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNotNumeric() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent("abc"));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNotValidLength() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent("123"));
	}
}