package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.sts.StsRestConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;

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
	public void ShouldValidateOrgNrWith9Numbers() {
		pdlIdentConsumer.validateFolkeregisterIdent("111111111");
	}

	@Test
	public void ShouldValidateFnrWith11Numbers() {
		pdlIdentConsumer.validateFolkeregisterIdent("11111111111");
	}

	@Test
	public void ShouldValidateAktoerIdWith13Numbers() {
		pdlIdentConsumer.validateFolkeregisterIdent("11111111111");
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNull() {
		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent(null));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsEmpty() {
		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent(""));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNotNumeric() {
		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent("abc"));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNotValidLength() {
		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.validateFolkeregisterIdent("123"));
	}
}