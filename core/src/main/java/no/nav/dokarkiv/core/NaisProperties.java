package no.nav.dokarkiv.core;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/// [nais texas](https://doc.nais.io/auth/reference/#texas)
@Data
@ConfigurationProperties("nais")
@Validated
public class NaisProperties {
	@NotEmpty
	private String tokenExchangeEndpoint;
	@NotEmpty
	private String tokenEndpoint;
}
