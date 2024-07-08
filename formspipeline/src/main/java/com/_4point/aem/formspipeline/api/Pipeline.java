package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public interface Pipeline<T, R> {
	
	public Stream<R> process(T dataChunk);
	public Stream<R> process(Stream<T> dataChunks);

	public interface PipelineFactory<T, R> {
		
		PipelineBuilder<T, R> builder(); 
	
		public interface PipelineBuilder<T, R> {
			Pipeline<T, R> build();
		}
	}
}

