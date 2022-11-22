package com._4point.aem.formspipeline.api;

public non-sealed interface OutputTransformationOneToOne<T extends OutputChunk<? extends Context, ? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> extends OutputTransformation {
	public R process(T outputChunk);
}
