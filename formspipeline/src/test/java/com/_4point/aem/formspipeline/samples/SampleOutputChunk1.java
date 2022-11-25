package com._4point.aem.formspipeline.samples;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.samples.SampleDataChunk2.SampleDataContext2;
import com._4point.aem.formspipeline.samples.SampleOutputChunk1.SampleOutputContext1;

public class SampleOutputChunk1 implements OutputChunk<SampleDataChunk2.SampleDataContext2, SampleOutputContext1> {
	public static final String DATA_PAYLOAD = "SampleDataContext1 Payload";
	public static final String CONTEXT_KEY = "SampleOutputKey1";
	public static final String CONTEXT_VALUE = "SampleOutputValue1";
	private final SampleOutputContext1 contextInstance = new SampleOutputContext1();
	private final SampleDataChunk2.SampleDataContext2 dataContext;

	public SampleOutputChunk1(SampleDataContext2 dataContext) {
		this.dataContext = dataContext;
	}

	public class SampleOutputContext1 implements Context {

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
	public SampleDataContext2 dataContext() {
		return dataContext;
	}

	@Override
	public SampleOutputContext1 outputContext() {
		return contextInstance;
	}
}
