package com._4point.aem.formspipeline.api;

public non-sealed interface DataTransformationOneToOne<T extends DataChunk<? extends Context>, R extends DataChunk<? extends Context>> extends DataTransformation {
	public R process(T dataChunk);
}
