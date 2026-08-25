package no.nav.dokarkiv.core.consumer.texas;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

public class NaisTexasRequestInterceptor implements ClientHttpRequestInterceptor {

	public static final String TARGET_SCOPE = "targetScope";
	public static final String TOKEN_FOR_EXCHANGE = "tokenForExchange";
	private final NaisTexasConsumer naisTexasConsumer;

	public NaisTexasRequestInterceptor(NaisTexasConsumer naisTexasConsumer) {
		this.naisTexasConsumer = naisTexasConsumer;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Map<String, Object> attributes = request.getAttributes();

		if (attributes.containsKey(TARGET_SCOPE)) {
			String targetScope = (String) attributes.get(TARGET_SCOPE);
			if(attributes.containsKey(TOKEN_FOR_EXCHANGE)) {
				String accessToken = (String) attributes.get(TOKEN_FOR_EXCHANGE);
				request.getHeaders().setBearerAuth(naisTexasConsumer.exchangeForTokenX(accessToken, targetScope));
			} else {
				request.getHeaders().setBearerAuth(naisTexasConsumer.getSystemToken(targetScope));
			}
		}

		return execution.execute(request, body);
	}

}