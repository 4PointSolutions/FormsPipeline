package com._4point.aem.formspipeline.samples;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.samples.SampleDataChunk2.SampleDataContext2;
import com._4point.aem.formspipeline.samples.SampleOutputChunk2.SampleOutputContext2;

public class SampleOutputChunk2 implements OutputChunk<SampleDataChunk2.SampleDataContext2, SampleOutputContext2> {
	public static final String DATA_PAYLOAD = "SampleDataContext2 Payload";
	public static final String CONTEXT_KEY = "SampleOutputKey2";
	public static final String CONTEXT_VALUE = "SampleOutputValue2";
	private final SampleOutputContext2 contextInstance = new SampleOutputContext2();
	private final SampleDataChunk2.SampleDataContext2 dataContext;

	public SampleOutputChunk2(SampleDataContext2 dataContext) {
		this.dataContext = dataContext;
	}

	public class SampleOutputContext2 implements Context {

		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			if (CONTEXT_KEY.equals(key) && target.isAssignableFrom(String.class)) {
				return Optional.of(target.cast(CONTEXT_VALUE));
			}
			return Optional.empty();
		}
		public String getSampleValue2() {
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
	public SampleOutputContext2 outputContext() {
		return contextInstance;
	}
}
