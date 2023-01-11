package com._4point.aem.formspipeline.chunks;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;

public class SimpleOutputChunk implements OutputChunk<Context, Context>  {
	private final byte[] bytes;
	private final Context dataContext;
	private final Context outpuContext;
	    	
	public SimpleOutputChunk(Context dataContext, Context outputContext, byte[] data) {
		bytes = data;
		this.dataContext = dataContext;
		this.outpuContext = outputContext;
	}

	@Override
	public byte[] bytes() {
		return bytes;
	}

	@Override
	public Context dataContext() {
		return dataContext;
	}

	@Override
	public Context outputContext() {
		return outpuContext;
	}
}
