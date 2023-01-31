package com._4point.aem.formspipeline.contexts;

import java.util.Objects;
import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;

/**
 * Special (but common) case where there is just one value in the Context.  This will be more efficient than using a MapContext.
 *
 */
public class SingletonContext implements Context {
	private final String key;
	private final Object value;

	public SingletonContext(String key, Object value) {
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public <T> Optional<T> get(String key2, Class<T> target) {
		if (key.equals(key2) && Objects.requireNonNull(target).isAssignableFrom(value.getClass())) {
			return Optional.of(target.cast(value));
		}
		return Optional.empty();
	}
}
