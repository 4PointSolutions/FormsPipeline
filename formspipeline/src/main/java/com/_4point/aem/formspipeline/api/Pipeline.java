package com._4point.aem.formspipeline.api;

import java.util.function.Function;

public interface Pipeline<T, R> extends Function<T, R> {

	public interface PipelineFactory<T, R> {
		
		PipelineBuilder<T, R> builder(); 
	
		public interface PipelineBuilder<T, R> {
			Pipeline<T, R> build();
		}
	}

}

