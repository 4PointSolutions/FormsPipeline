package com._4point.aem.formspipeline.api;

public interface Result<D extends Context, O extends Context, R extends Context> {
	public D dataContext();
	public O outputContext();
	public R resultContext();
}
