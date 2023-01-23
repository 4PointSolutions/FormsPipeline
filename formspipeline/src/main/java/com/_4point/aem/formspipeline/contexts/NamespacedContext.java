package com._4point.aem.formspipeline.contexts;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;

public class NamespacedContext implements Context {
	private static final String DEFAULT_NAMESPACE_SEPARATOR = "."; 
	
	private final String prefix;
	private final Context originalContext;
	
	public NamespacedContext(String namespace, String nsSeparator, Context originalContext) {
		this.prefix = namespace + nsSeparator;
		this.originalContext = originalContext;
	}

	public NamespacedContext(String namespace, Context originalContext) {
		this(namespace, DEFAULT_NAMESPACE_SEPARATOR, originalContext);
	}

	@Override
	public <T> Optional<T> get(String key, Class<T> target) {
		if (key.startsWith(prefix)) {
			return originalContext.get(key.substring(prefix.length()), target);
		}
		return Optional.empty();
	}

}
