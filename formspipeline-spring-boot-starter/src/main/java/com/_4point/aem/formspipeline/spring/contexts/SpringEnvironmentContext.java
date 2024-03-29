package com._4point.aem.formspipeline.spring.contexts;

import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com._4point.aem.formspipeline.api.Context;

@Component
public class SpringEnvironmentContext implements Context {
	
	private final Environment springEnvironment;

	public SpringEnvironmentContext(Environment springEnvironment) {
		this.springEnvironment = springEnvironment;
	}

	@Override
	public <T> Optional<T> get(String key, Class<T> target) {
		T value = springEnvironment.getProperty(key, target);		
		return Optional.ofNullable(value);
	}	
}
