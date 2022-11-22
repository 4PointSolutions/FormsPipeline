package com._4point.aem.formspipeline.api;

import java.util.Optional;

public interface Context {
	<T> Optional<T> get(String key, Class<T> target);
	default Optional<String> getString(String key) { return get(key, String.class); }
}
