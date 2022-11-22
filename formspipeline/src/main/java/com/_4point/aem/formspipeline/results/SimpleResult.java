package com._4point.aem.formspipeline.results;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Result;

public class SimpleResult<D extends Context, O extends Context, R extends Context> implements Result<D, O, R> {
	private final D dataContext;
	private final O outputContext;
	private final R resultContext;
	
	public SimpleResult(D dataContext, O outputContext, R resultContext) {
		super();
		this.dataContext = dataContext;
		this.outputContext = outputContext;
		this.resultContext = resultContext;
	}

	@Override
	public D dataContext() {
		return dataContext;
	}

	@Override
	public O outputContext() {
		return outputContext;
	}

	@Override
	public R resultContext() {
		return resultContext;
	}

}
