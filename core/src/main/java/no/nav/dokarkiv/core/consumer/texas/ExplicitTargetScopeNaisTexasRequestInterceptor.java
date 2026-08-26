package no.nav.dokarkiv.core.consumer.texas;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class ExplicitTargetScopeNaisTexasRequestInterceptor implements ClientHttpRequestInterceptor {
	private final NaisTexasConsumer naisTexasConsumer;
	private final String targetScope;

	public ExplicitTargetScopeNaisTexasRequestInterceptor(NaisTexasConsumer naisTexasConsumer, String targetScope) {
		this.naisTexasConsumer = naisTexasConsumer;
		this.targetScope = targetScope;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().setBearerAuth(naisTexasConsumer.getSystemToken(targetScope));
		return execution.execute(request, body);
	}
}
