package no.nav.dokarkiv.core.domain.builder;

/**
 * Base class for builders.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @param <T> The object to build.
 */
@Deprecated // bruk lombok builder istedet
public abstract class Builder<T> {
	public abstract T build();
}