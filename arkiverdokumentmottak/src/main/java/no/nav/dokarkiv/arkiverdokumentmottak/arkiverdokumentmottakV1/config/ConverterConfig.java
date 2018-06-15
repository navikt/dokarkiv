package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.config;

import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.ArkiverDokumentmottakTilleggsopplysningerConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
public class ConverterConfig {

	@Bean
	public ArkiverDokumentmottakTilleggsopplysningerConverter arkiverDokumentmottakTilleggsopplysningerConverter() {
		return new ArkiverDokumentmottakTilleggsopplysningerConverter();
	}
}
