package no.nav.dokarkiv.core.consumer.aktoer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import no.nav.dokarkiv.core.security.STSConfig;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class AktoerV2Config {
	private static final int CACHE_MAX_SIZE = 1000;
	private static final int CACHE_EXPIRES_AFTER = 10;

	@Bean
	public AktoerV2 aktoerV2(STSConfig stsConfig) {
		JaxWsProxyFactoryBean clientFactory = new JaxWsProxyFactoryBean();
		clientFactory.setServiceClass(AktoerV2.class);
		clientFactory.setAddress("http://tmp");
		clientFactory.setFeatures(Collections.singletonList(new WSAddressingFeature()));
		AktoerV2 aktoerV2 = (AktoerV2) clientFactory.create();
		stsConfig.configureSTS(aktoerV2);
		return aktoerV2;
	}

	@Bean
	public Cache<String, HentAktoerIdForIdentResponse> aktoerResponseCache() {
		return CacheBuilder.newBuilder()
				.expireAfterAccess(CACHE_EXPIRES_AFTER, TimeUnit.MINUTES)
				.maximumSize(CACHE_MAX_SIZE)
				.build();
	}
}
