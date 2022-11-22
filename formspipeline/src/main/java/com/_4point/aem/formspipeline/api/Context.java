package com._4point.aem.formspipeline.api;

public interface Context {
	<T> T get(String key, Class<T> target);
	default String getString(String key) { return get(key, String.class); }
}
