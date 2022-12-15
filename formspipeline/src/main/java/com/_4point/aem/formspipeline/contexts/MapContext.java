package com._4point.aem.formspipeline.contexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com._4point.aem.formspipeline.api.Context;

public class MapContext implements Context {
	private Map<String, ? extends Object> contextMap;
	
	public MapContext(Map<String, ? extends Object> contextMap) {
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
		MapContext other = (MapContext) obj;
		return Objects.equals(contextMap, other.contextMap);
	}

	@Override
	public String toString() {
		return "MapContext [contextMap=" + contextMap + "]";
	}
	
	public static MapContextBuilder builder() { return new MapContextBuilder(); }
	
	public static class MapContextBuilder implements ContextBuilder {
		private record Entry(String key, Object value) {}
		
		private final List<Entry> list = new ArrayList<>();
		
		public MapContextBuilder put(String key, Object value) {
			// add this entry to our list
			list.add(new Entry(key, value));
			return this;
		}
		
		public Context build() {
			// Convert the list to a Map and then return a MapContext constructed with that map.
			return new MapContext(list.stream().collect(Collectors.toMap(e->e.key(), e->e.value())));
		}
	}
}
