package no.nav.dokarkiv.core.config;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Spring configuration of Dozer for MOD services.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Configuration
public class DozerConfig {


	private static final String ARKIVER_DOKUMENTMOTTAK_MAPPING_XML = "modules/prv-arkiverdokumentmottak-mapping.xml";

	@Bean
	public Mapper dozerMapper() {
		DozerBeanMapper beanMapper = new DozerBeanMapper();
		beanMapper.setMappingFiles(Collections.singletonList(ARKIVER_DOKUMENTMOTTAK_MAPPING_XML));

		return beanMapper;
	}


}