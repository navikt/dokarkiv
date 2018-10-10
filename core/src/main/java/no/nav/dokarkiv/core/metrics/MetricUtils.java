package no.nav.dokarkiv.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class MetricUtils {


    public static void incrementExceptionCounter(String counterName, Throwable throwable, MeterRegistry meterRegistry, String... otherParameters) {
        Counter.builder(counterName)
                .tags("error_type", isFunctionalException(throwable) ? "functional" : "technical")
                .tags("exception_name", throwable.getClass().getSimpleName())
                .tags(otherParameters)
                .register(meterRegistry)
                .increment();
    }

    public static void incrementCounter(MeterRegistry meterRegistry, String counterName, String... otherParameters) {
        Counter.builder(counterName)
                .tags(otherParameters)
                .register(meterRegistry)
                .increment();
    }

    public static boolean isFunctionalException(Throwable e) {
        return e instanceof DokarkivFunctionalException || e instanceof AuthorizationException;
    }

}
