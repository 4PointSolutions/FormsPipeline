package com._4point.aem.formspipeline.spring.chunks;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;

public abstract class XmlDataChunk<T extends Context> implements DataChunk<T> {

	@Override
	public byte[] bytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	abstract public T dataContext();
}
