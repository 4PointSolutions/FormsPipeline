package com._4point.aem.formspipeline.api;

public interface OutputDestination<
								   T extends OutputChunk<? extends Context, ? extends Context>, 
								   R extends Result<? extends Context, ? extends Context, ? extends Context>
								  > {
	R process(T outputChunk);
}
