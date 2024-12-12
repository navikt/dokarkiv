package no.nav.dokarkiv.core.security.azure;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("azure")
@Configuration
@Validated
public class AzureConfig {
	@NotEmpty
	private String openidConfigTokenEndpoint;
	@NotEmpty
	private String appClientId;
	@NotEmpty
	private String appClientSecret;
	@NotEmpty
	private String appTenantId;
}
