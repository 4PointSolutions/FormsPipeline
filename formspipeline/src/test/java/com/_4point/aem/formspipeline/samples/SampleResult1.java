package com._4point.aem.formspipeline.samples;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.samples.SampleDataChunk2.SampleDataContext2;
import com._4point.aem.formspipeline.samples.SampleOutputChunk2.SampleOutputContext2;
import com._4point.aem.formspipeline.samples.SampleResult1.SampleResultContext1;

public class SampleResult1 implements Result<SampleDataChunk2.SampleDataContext2, SampleOutputChunk2.SampleOutputContext2, SampleResultContext1> {
	public static final String CONTEXT_KEY = "SampleResultKey1";
	public static final String CONTEXT_VALUE = "SampleResultValue1";
	private final SampleResultContext1 contextInstance = new SampleResultContext1();
	private final SampleDataChunk2.SampleDataContext2 dataContext;
	private final SampleOutputChunk2.SampleOutputContext2 outputContext;

	public SampleResult1(SampleDataContext2 dataContext, SampleOutputContext2 outputContext) {
		this.dataContext = dataContext;
		this.outputContext = outputContext;
	}

	public class SampleResultContext1 implements Context {
		
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
	public SampleDataContext2 dataContext() {
		return dataContext;
	}

	@Override
	public SampleOutputContext2 outputContext() {
		return outputContext;
	}

	@Override
	public SampleResultContext1 resultContext() {
		return contextInstance;
	}


}
