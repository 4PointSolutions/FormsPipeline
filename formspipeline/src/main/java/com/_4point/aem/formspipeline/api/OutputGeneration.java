package com._4point.aem.formspipeline.api;

public interface OutputGeneration<T extends DataChunk<? extends Context>, R extends OutputChunk<? extends Context, ? extends Context>> {
	R process(T dataChunk);
}
