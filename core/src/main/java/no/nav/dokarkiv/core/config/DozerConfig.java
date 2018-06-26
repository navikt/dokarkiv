package no.nav.dokarkiv.core.config;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Spring configuration of Dozer for MOD services.
 *
 * @deprecated Vi skal slutte å bruke dozer
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Configuration
@Deprecated
public class DozerConfig {

	private static final String ARKIVER_DOKUMENTMOTTAK_MAPPING_XML = "prv-arkiverdokumentmottak-mapping.xml";
	private static final String ARKIVER_DOKUMENTPRODUKSJON_V2_MAPPING_XML = "prv-arkiverdokumentmottakV2-mapping.xml";
	private static final String ARKIVER_DOKUMENTPRODUKSJON_MAPPING_XML = "prv-arkiverdokumentproduksjon-mapping.xml";

	@Bean
	public Mapper dozerMapper() {
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		beanMapper.setMappingFiles(Arrays.asList(
				ARKIVER_DOKUMENTMOTTAK_MAPPING_XML,
				ARKIVER_DOKUMENTPRODUKSJON_MAPPING_XML,
				ARKIVER_DOKUMENTPRODUKSJON_V2_MAPPING_XML
		));
		return beanMapper;
	}

}