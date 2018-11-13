package no.nav.dokarkiv.core.consumer.aktoer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import no.nav.dokarkiv.core.security.STSConfig;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
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
	public AktoerV2 aktoerV2(STSConfig stsConfig,
							 @Value("${aktoerv2.url}") String aktoerV2Url) {
		JaxWsProxyFactoryBean clientFactory = new JaxWsProxyFactoryBean();
		clientFactory.setServiceClass(AktoerV2.class);
		clientFactory.setAddress(aktoerV2Url);
		clientFactory.setFeatures(Collections.singletonList(new WSAddressingFeature()));
		AktoerV2 aktoerV2 = (AktoerV2) clientFactory.create();
		stsConfig.configureSTS(aktoerV2);
		Client client = ClientProxy.getClient(aktoerV2);
		setClientTimeout(client);
		return aktoerV2;
	}

	private void setClientTimeout(Client client) {
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(3000L);
		httpClientPolicy.setReceiveTimeout(3000L);
		conduit.setClient(httpClientPolicy);
	}

	@Bean
	public Cache<String, HentAktoerIdForIdentResponse> aktoerResponseCache() {
		return CacheBuilder.newBuilder()
				.expireAfterAccess(CACHE_EXPIRES_AFTER, TimeUnit.MINUTES)
				.maximumSize(CACHE_MAX_SIZE)
				.build();
	}

	@Bean
	public Cache<String, HentIdentForAktoerIdResponse> identResponseCache() {
		return CacheBuilder.newBuilder()
				.expireAfterAccess(CACHE_EXPIRES_AFTER, TimeUnit.MINUTES)
				.maximumSize(CACHE_MAX_SIZE)
				.build();
	}

}
