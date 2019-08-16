/*
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.nav.dokarkiv.core.metrics;

import static java.util.Arrays.asList;

import io.micrometer.core.annotation.Incubating;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.function.Function;


/**
 * AspectJ aspect for intercepting types or method annotated with @Timed.
 * Changes: Counter for exceptions
 *
 * @author Joakim Bjørnstad, Jbit AS
 * @author David J. M. Karlsen
 * @author Jon Schneider
 */
@Aspect
@NonNullApi
@Incubating(since = "1.0.0")
@Slf4j
@SuppressWarnings("Duplicates")
public class DokTimedAspect {
	private final MeterRegistry registry;
	private final Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint;

	public DokTimedAspect(MeterRegistry registry) {
		this(registry, pjp ->
				Tags.of("class", pjp.getStaticPart().getSignature().getDeclaringTypeName(),
						"method", pjp.getStaticPart().getSignature().getName())
		);
	}

	public DokTimedAspect(MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinpoint) {
		this.registry = registry;
		this.tagsBasedOnJoinpoint = tagsBasedOnJoinpoint;
	}

	@Around("execution (@io.micrometer.core.annotation.Timed * *.*(..))")
	public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		Timed timed = method.getAnnotation(Timed.class);

		if (timed.value().isEmpty()) {
			return pjp.proceed();
		}

		Timer.Sample sample = Timer.start(registry);
		try {
			return pjp.proceed();
		} catch (Exception e) {
			logException(method, e);

			Counter.builder(timed.value() + "_exception")
					.tags("error_type", isFunctionalException(method, e) ? "functional" : "technical")
					.tags("exception_name", e.getClass().getSimpleName())
					.tags(timed.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.register(registry)
					.increment();
			throw e;
		} finally {
			sample.stop(Timer.builder(timed.value())
					.description(timed.description().isEmpty() ? null : timed.description())
					.tags(timed.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.publishPercentileHistogram(timed.histogram())
					.publishPercentiles(timed.percentiles().length == 0 ? null : timed.percentiles())
					.register(registry));
		}
	}

	@Around("execution (@no.nav.dokarkiv.core.metrics.SakMetrics * *.*(..))")
	public Object restMetrics(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		SakMetrics restMetrics = method.getAnnotation(SakMetrics.class);
		if (restMetrics.value().isEmpty()) {
			return pjp.proceed();
		}

		Timer.Sample sample = Timer.start(registry);
		try {
			return pjp.proceed();
		} catch (Exception e) {

			logException(method, e);

			Counter.builder(restMetrics.value() + "_exception")
					.tags("error_type", isFunctionalException(method, e) ? "functional" : "technical")
					.tags("exception_name", e.getClass().getSimpleName())
					.tags(restMetrics.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.register(registry)
					.increment();

			throw e;

		} finally {
			sample.stop(Timer.builder(restMetrics.value())
					.description(restMetrics.description().isEmpty() ? null : restMetrics.description())
					.tags(restMetrics.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.publishPercentileHistogram(restMetrics.histogram())
					.publishPercentiles(restMetrics.percentiles().length == 0 ? null : restMetrics.percentiles())
					.register(registry));
		}
	}

	@Around("execution (@no.nav.dokarkiv.core.metrics.GraphQLMetrics * *.*(..))")
	public Object graphQLMetrics(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		GraphQLMetrics graphQLMetrics = method.getAnnotation(GraphQLMetrics.class);
		if (graphQLMetrics.value().isEmpty()) {
			return pjp.proceed();
		}

		MetricUtils.incrementCounter(registry, graphQLMetrics.value(), graphQLMetrics.extraTags());
		return pjp.proceed();

	}


	@Around("execution (@no.nav.dokarkiv.core.metrics.SakMetrics * *.*(..))")
	public Object sakMetrics(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		SakMetrics sakMetrics = method.getAnnotation(SakMetrics.class);
		if (sakMetrics.value().isEmpty()) {
			return pjp.proceed();
		}

		Timer.Sample sample = Timer.start(registry);
		try {
			return pjp.proceed();
		} catch (Exception e) {

			logException(method, e);

			Counter.builder(sakMetrics.value() + "_exception")
					.tags("error_type", isFunctionalException(method, e) ? "functional" : "technical")
					.tags("exception_name", e.getClass().getSimpleName())
					.tags(sakMetrics.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.register(registry)
					.increment();

			throw e;

		} finally {
			sample.stop(Timer.builder(sakMetrics.value())
					.description(sakMetrics.description().isEmpty() ? null : sakMetrics.description())
					.tags(sakMetrics.extraTags())
					.tags(tagsBasedOnJoinpoint.apply(pjp))
					.publishPercentileHistogram(sakMetrics.histogram())
					.publishPercentiles(sakMetrics.percentiles().length == 0 ? null : sakMetrics.percentiles())
					.register(registry));
		}
	}


	private boolean isFunctionalException(Method method, Exception e) {
		return asList(method.getExceptionTypes()).contains(e.getClass()) || MetricUtils.isFunctionalException(e);
	}

	private void logException(Method method, Exception e) {
		String mdcRequestId = (MDC.get(MDCConstants.MDC_REQUEST_ID) == null) ? "" : (MDC.get(MDCConstants.MDC_REQUEST_ID) + " ");

		if (isFunctionalException(method, e)) {
			log.warn(mdcRequestId + e.getMessage(), e);
		} else {
			log.error(mdcRequestId + e.getMessage(), e);
		}
	}


	private enum MetricsType {
		TIMED,
		REST_METRICS
	}
}
