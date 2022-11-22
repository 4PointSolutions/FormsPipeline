package com._4point.aem.formspipeline.contexts;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;

public class HashMapContext implements Context {
	private Map<String, ? extends Object> contextMap;
	
	public HashMapContext(Map<String, ? extends Object> contextMap) {
		this.contextMap = Map.copyOf(contextMap);
	}

	@Override
	public <T> Optional<T> get(String key, Class<T> target) {
		Object object = contextMap.get(key);
		if (object == null) {
			return Optional.empty();
		}
		if (target.isInstance(object)) {
			return Optional.of(target.cast(object));
		}
		throw new IllegalArgumentException("Unable to convert object of type " + object.getClass().getName() + " to " + target.getName() + ".");
	}

	@Override
	public int hashCode() {
		return Objects.hash(contextMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashMapContext other = (HashMapContext) obj;
		return Objects.equals(contextMap, other.contextMap);
	}

	@Override
	public String toString() {
		return "HashMapContext [contextMap=" + contextMap + "]";
	}
}
