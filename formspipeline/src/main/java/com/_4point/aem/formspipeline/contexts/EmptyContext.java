package com._4point.aem.formspipeline.contexts;

import java.util.List;
import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;

/**
 * Empty context contains no entries and always returns empty Optional.
 *
 */
public class EmptyContext implements Context {
	
	private static final EmptyContext INSTANCE = new EmptyContext(); 

	@Override
	public <T> Optional<T> get(String key, Class<T> target) {
		return Optional.empty();
	}

	@Override
	public <T> List<T> getMulti(String key, Class<T> target) {
		return List.of();
	}
	
	/**
	 * Always returns the same EmptyContext object.
	 * 
	 * @return
	 */
	public static EmptyContext emptyInstance() {
		return INSTANCE;
	}
}
