package com._4point.aem.formspipeline.chunks;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;

public class SimpleDataChunk implements DataChunk<Context>  {
	private final byte[] bytes;
	private final Context dataContext;
	    	
	public SimpleDataChunk(Context dataContext,byte[] data) {
		bytes = data;
		this.dataContext = dataContext;
	}
	@Override
	public byte[] bytes() {
		return bytes;
	}

	@Override
	public Context dataContext() {
		return dataContext;
	}

}
