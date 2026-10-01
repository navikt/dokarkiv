package no.nav.dokarkiv.core.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/// Eksponert av Nais plattformen
@Data
@ConfigurationProperties("google.cloud")
@Validated
public class GoogleCloudProperties {
	@NotBlank
	private String project;
}
