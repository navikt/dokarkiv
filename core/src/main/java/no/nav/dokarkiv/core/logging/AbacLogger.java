package no.nav.dokarkiv.core.logging;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import lombok.extern.slf4j.Slf4j;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Component
@Slf4j(topic = "abacLogger")
public class AbacLogger {
	private static final String SEPARATOR = ".";

	public void logAbacDeny(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse, final Map<String, String> resources) {
		log.warn("{}, {}; {}", mapRequest(xacmlRequest), mapCustomResources(resources), mapResponse(xacmlResponse));
	}

	public void logAbacPermit(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse, final Map<String, String> resources) {
		log.info("{}, {}; {}", mapRequest(xacmlRequest), mapCustomResources(resources), mapResponse(xacmlResponse));
	}

	private String mapCustomResources(final Map<String, String> resources) {
		return resources.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(", "));
	}

	private String mapRequest(final XacmlRequest xacmlRequest) {
		return "Authorization Request: " +
				xacmlRequest.getResources()
						.stream()
						.map(xacmlAttribute -> substringAfterLast(xacmlAttribute.getAttributeId(), SEPARATOR) + "=" + xacmlAttribute.getValue())
						.collect(Collectors.joining(", "));
	}

	private String mapResponse(final XacmlResponse xacmlResponse) {
		return "Authorization Response: " +
				"decision=" + xacmlResponse.getDecision().getValue() +
				(
						xacmlResponse.getAdvices().isEmpty() ? "" :
								", " + xacmlResponse.getAdvices()
										.stream()
										.flatMap(a -> a.getAttributeAssignments().stream())
										.map(as -> substringAfterLast(as.getAttributeId(), SEPARATOR) + "=" + as.getValue())
										.collect(Collectors.joining(", "))
				);
	}
}
