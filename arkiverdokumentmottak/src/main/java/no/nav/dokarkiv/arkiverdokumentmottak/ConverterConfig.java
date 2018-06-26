package no.nav.dokarkiv.arkiverdokumentmottak;

import no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203.ArkiverDokumentmottakTilleggsopplysningerConverter;
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
