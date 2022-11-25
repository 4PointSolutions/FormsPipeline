package com._4point.aem.formspipeline.samples;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.samples.SampleDataChunk1.SampleDataContext1;

public class SampleDataChunk1 implements DataChunk<SampleDataContext1> {
	public static final String DATA_PAYLOAD = "SampleDataContext1 Payload";
	public static final String CONTEXT_KEY = "SampleKey1";
	public static final String CONTEXT_VALUE = "SampleValue1";
	private final SampleDataContext1 contextInstance = new SampleDataContext1();
	
	public class SampleDataContext1 implements Context {
		
		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			if (CONTEXT_KEY.equals(key) && target.isAssignableFrom(String.class)) {
				return Optional.of(target.cast(CONTEXT_VALUE));
			}
			return Optional.empty();
		}
		
		public String getSampleValue1() {
			return CONTEXT_VALUE;
		}
	}

	@Override
	public byte[] bytes() {
		return DATA_PAYLOAD.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public SampleDataContext1 dataContext() {
		return contextInstance;
	}
}
