package no.nav.dokarkiv.core.stelvio;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Defines a RedeployableThreadLocalSubstitute.
 * 
 * @author PC
 * 
 * @param <T>
 *            value type
 */
public class RedeployableThreadLocalSubstitute<T> {
	private final Map<Thread, T> valuesByThread = Collections.synchronizedMap(new WeakHashMap<Thread, T>());

	/**
	 * Creates a new instance of RedeployableThreadLocalSubstitute.
	 *
	 */
	public RedeployableThreadLocalSubstitute() {
	}

	/**
	 * Return true if value is set.
	 * 
	 * @return true if value is set
	 */
	public boolean isValueSet() {
		return get() != null;
	}

	/**
	 * Get value.
	 * 
	 * @return value
	 */
	public T get() {
		return valuesByThread.get(Thread.currentThread());
	}

	/**
	 * Set value.
	 * 
	 * @param value value
	 */
	public void set(T value) {
		if (value == null) {
			remove();
		} else {
			valuesByThread.put(Thread.currentThread(), value);
		}
	}

	/**
	 * Remove value.
	 */
	public void remove() {
		valuesByThread.remove(Thread.currentThread());
	}
}