package no.nav.dokarkiv.core.metrics;

import io.prometheus.client.Histogram;
import no.nav.dokarkiv.core.MDCConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@EnableApiFilters
public class PrometheusFilter implements ContainerRequestFilter, ContainerResponseFilter {


	private static final Histogram requestsHistogram = Histogram.build("requests_duration_seconds", "Request duration in seconds")
			.labelNames("path", "queryparams", "methods", "consumers")
			.register();
	private static final String PROMETHEUS_TIMER = "prometheus_timer";

	@Override
	public void filter(ContainerRequestContext containerRequestContext) throws IOException {
		UriInfo uriInfo = containerRequestContext.getUriInfo();
		String uriPath = uriInfo.getRequestUri().getPath();
		MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
		String sanitizedPath;
		String apiPrefix = "/rest";

		if(uriPath.startsWith(apiPrefix)){
			sanitizedPath = apiPrefix + replacePathParams(StringUtils.remove(uriPath,apiPrefix),pathParameters);
		} else {
			sanitizedPath=uriPath;
		}

		String queryParams = "N/A";
		if (!uriInfo.getQueryParameters().isEmpty()) {
			queryParams = uriInfo.getQueryParameters().keySet().toString();
		}
		Histogram.Timer timer = requestsHistogram.labels(sanitizedPath,
				queryParams,
				containerRequestContext.getMethod(),
				(String) containerRequestContext.getProperty(MDC.get(MDCConstants.MDC_CONSUMER_ID))).startTimer();
		containerRequestContext.setProperty(PROMETHEUS_TIMER, timer);

	}

	private String replacePathParams(String uriPath, MultivaluedMap<String, String> pathParameters) {
		String modifiedPath = uriPath;
		for(Map.Entry<String, List<String>> entry: pathParameters.entrySet()){
			String originalPathFragment = String.format("{%s}", entry.getKey());
			modifiedPath= StringUtils.replaceEach(uriPath,entry.getValue().toArray(new String[0]),new String[]{
					originalPathFragment
			});
		}
		return modifiedPath;
	}


	@Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
		Histogram.Timer timer = Histogram.Timer.class.cast(containerRequestContext.getProperty(PROMETHEUS_TIMER));
		if (timer != null) {
			timer.observeDuration();
		}

	}
}
