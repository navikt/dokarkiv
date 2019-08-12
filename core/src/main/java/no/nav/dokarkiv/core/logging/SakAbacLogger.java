package no.nav.dokarkiv.core.logging;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Component
@Slf4j(topic = "sakabaclogger")
public class SakAbacLogger {

	public void logAbacDeny(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse, final Map<String, String> resources) {
		log.warn("ConsumerID: {}; User: {}; Endpoint: {}; Method: {}; Authorization Request: {}; Authorization Response: {}",
				MDC.get(MDCConstants.MDC_CONSUMER_ID),
				MDC.get(MDCConstants.MDC_USER_ID),
				getAbsolutePath(),
				getRequestMethod(),
				mapRequest(xacmlRequest),
				mapResponse(xacmlResponse));
	}

	public void logAbacPermit(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse, final Map<String, String> resources) {
		log.info("ConsumerID: {}; User: {}; Endpoint: {}; Method: {}; Authorization Request: {}; Authorization Response: {}",
				MDC.get(MDCConstants.MDC_CONSUMER_ID),
				MDC.get(MDCConstants.MDC_USER_ID),
				getAbsolutePath(),
				getRequestMethod(),
				mapRequest(xacmlRequest),
				mapResponse(xacmlResponse));
	}

	public void logAbacWarn(String msg) {
		log.warn(msg);
	}

	private String getAbsolutePath() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURL().toString();
	}

	private String getRequestMethod() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getMethod();
	}

	private String getAttributeIdFromResource(String resource) {
		String[] split = resource.split("\\.");
		return split[split.length - 1];
	}

	private String mapRequest(final XacmlRequest xacmlRequest) {
		return xacmlRequest.getResources().stream()
				.map(resource -> String.format("%s=%s", getAttributeIdFromResource(resource.getAttributeId()), resource.getValue()))
				.collect(Collectors.joining(", "));
	}

	private String mapDecision(XacmlResponse xacmlResponse) {
		return xacmlResponse.getAdvices().isEmpty() ? "" : xacmlResponse.getAdvices().get(0).getAttributeAssignments().stream()
				.map(dec -> String.format("%s=%s", getAttributeIdFromResource(dec.getAttributeId()), dec.getValue()))
				.collect(Collectors.joining(", "));
	}

	private String mapResponse(final XacmlResponse xacmlResponse) {
		String response = String.format("decision=%s, %s", xacmlResponse.getDecision().getValue(), mapDecision(xacmlResponse));
		if (xacmlResponse.getAdvices().isEmpty()) {
			response = StringUtils.remove(response, ",");
		}

		return response;
	}
}
