package no.nav.dokarkiv;

import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.consumer.azure.TokenConsumer;
import no.nav.dokarkiv.core.consumer.azure.TokenResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class TestConfig {

	@Bean
	public TokenConsumer tokenConsumer() {
		return (TokenConsumer) token -> new TokenResponse();
	}

	@Bean
	public AzureAdGraphService azureAdGraphService() {
		AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
		when(azureAdGraphServiceMock.hentFulltNavn(any())).thenReturn("User Name");
		return azureAdGraphServiceMock;
	}
}
