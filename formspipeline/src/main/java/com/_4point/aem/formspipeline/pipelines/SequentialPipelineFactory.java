package com._4point.aem.formspipeline.pipelines;

import java.util.function.Function;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;

public class SequentialPipelineFactory<T, R> /* implements PipelineFactory */ {
	private Function<Message<T>, Message<R>> fn;
	
	public SequentialPipelineFactory(Function<Message<T>, Message<R>> fn) {
		this.fn = fn;
	}

	public <V> SequentialPipelineFactory<T, V> transform(Function<Message<R>, Message<V>> fn2) {
		return new SequentialPipelineFactory<T, V>(fn.andThen(fn2));
	}
	
	public <V> SequentialPipelineFactory<T, V> transformPayload(Function<R, V> fn2) {
		return new SequentialPipelineFactory<T, V>(fn.andThen(MessageBuilder.transformPayload(fn2)));
	}
	
	public SequentialPipelineFactory<T, R> transformContext(Function<? super Context, ? extends Context> fn2) {
		return new SequentialPipelineFactory<T, R>(fn.andThen(MessageBuilder.transformContext(fn2)));
	}
	
	// Functions to add
	// Split
	// Aggregate
	// Route
	
//	@Override
//	public SequentialPipelineBuilder builder() {
//		return new SequentialPipelineBuilder();
//	}
//
//	public static class SequentialPipelineBuilder /* implements PipelineBuilder */ {
//
//		@Override
//		public Function<> build() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
//
//	private static class SequentialPipeline{
//		
//	}
}
