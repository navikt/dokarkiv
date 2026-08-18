package no.nav.dokarkiv.core.consumer.texas;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;
import static no.nav.dokarkiv.core.NavHeaders.NAV_CALL_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

		request.getHeaders().add(NAV_CALL_ID, getMDCCallId());

		return execution.execute(request, body);
	}

	private static String getMDCCallId() {
		String callId = MDC.get(MDC_CALL_ID);
		return isNotBlank(callId) ? callId : UUID.randomUUID().toString();
	}

}