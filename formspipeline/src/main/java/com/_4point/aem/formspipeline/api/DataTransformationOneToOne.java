package com._4point.aem.formspipeline.api;

public non-sealed interface DataTransformationOneToOne<T extends DataChunk, R extends DataChunk> extends DataTransformation {
	public R process(T dataChunk);
}
