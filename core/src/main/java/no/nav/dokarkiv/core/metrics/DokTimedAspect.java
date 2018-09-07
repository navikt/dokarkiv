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
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
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
			if (isFunctionalException(method, e)) {
				log.warn(e.getMessage(), e);
			} else {
				log.error(e.getMessage(), e);
			}
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

	@Around("execution (@no.nav.dokarkiv.core.metrics.RestMetrics * *.*(..))")
	public Object restMetrics(ProceedingJoinPoint pjp) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();

		RestMetrics timed = method.getAnnotation(RestMetrics.class);
		if (timed.value().isEmpty()) {
			return pjp.proceed();
		}

		Timer.Sample sample = Timer.start(registry);
		try {
			return pjp.proceed();
		} catch (Exception e) {
			if (isFunctionalException(method, e)) {
				log.warn(e.getMessage(), e);
			} else {
				log.error(e.getMessage(), e);
			}
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

	private boolean isFunctionalException(Method method, Exception e) {
		return asList(method.getExceptionTypes()).contains(e.getClass()) || e instanceof DokarkivFunctionalException;
	}


	private enum MetricsType{
		TIMED,
		REST_METRICS
	}
}
