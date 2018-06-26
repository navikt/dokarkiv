package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.config;


import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.DefaultJournalforInngaaendeForsendelseRequestMapper;
import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.DefaultJournalforInngaaendeForsendelseResponseMapper;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the ArkiverDokumentMottaktV1 ws service
 *
 * @author Stig Strøm
 */
@Configuration
public class ArkiverDokumentmottakConfig {

	public static final String PROVIDER_BEAN = "prv.joark.nsb.arkiverDokumentmottak";
	public static final String PROVIDER_BEAN_V2 = "prv.joark.nsb.arkiverDokumentmottakV2";

	@Bean(name = PROVIDER_BEAN)
	public ArkiverDokumentmottakV1 arkiverDokumentmottakProvider() {
		return new ArkiverDokumentmottakProvider();
	}

//    @Bean(name = PROVIDER_BEAN_V2)
//    public ArkiverDokumentmottakV2 arkiverDokumentmottakV2Provider() {
//        return new  DefaultArkiverDokumentmottakV2Provider();
//    }

	@Bean
	public DefaultJournalforInngaaendeForsendelseResponseMapper journalforInngaaendeForsendelseResponseMapper() {
		return new DefaultJournalforInngaaendeForsendelseResponseMapper();
	}

	@Bean
	public DefaultJournalforInngaaendeForsendelseRequestMapper journalforInngaaendeForsendelseRequestMapper() {
		return new DefaultJournalforInngaaendeForsendelseRequestMapper();
	}

//    @Bean
//    public  DefaultJournalforInngaaendeForsendelseV2ResponseMapper journalforInngaaendeForsendelseV2ResponseMapper() {
//        return new  DefaultJournalforInngaaendeForsendelseV2ResponseMapper();
//    }
//
//    @Bean
//    public  DefaultJournalforInngaaendeForsendelseV2RequestMapper journalforInngaaendeForsendelseV2RequestMapper() {
//    	return new  DefaultJournalforInngaaendeForsendelseV2RequestMapper();
//    }

	@Bean
	public DefaultArkiverDokumentmottakFaultInfoPopulator arkiverDokumentmottakFaultInfoPopulator() {
		return new DefaultArkiverDokumentmottakFaultInfoPopulator();
	}

//    @Bean
//    public  DefaultArkiverDokumentmottakV2FaultInfoPopulator arkiverDokumentmottakV2FaultInfoPopulator() {
//        return new DefaultArkiverDokumentmottakV2FaultInfoPopulator();
//    }
}
