package com._4point.aem.formspipeline.api;

public non-sealed interface OutputTransformationOneToOne<T extends OutputChunk, R extends OutputChunk> extends OutputTransformation {
	public R process(T outputChunk);
}
