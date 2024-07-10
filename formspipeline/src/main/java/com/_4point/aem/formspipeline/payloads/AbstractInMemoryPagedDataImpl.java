package com._4point.aem.formspipeline.payloads;

import java.util.OptionalInt;

import com._4point.aem.formspipeline.api.PagedData;

public abstract class AbstractInMemoryPagedDataImpl extends AbstractInMemoryTypedDataImpl implements PagedData {
	private final OptionalInt numPages;
	
	protected AbstractInMemoryPagedDataImpl(byte[] bytes, String contentType) {
		super(bytes, contentType);
		this.numPages = OptionalInt.empty();
	}

	protected AbstractInMemoryPagedDataImpl(byte[] bytes, String contentType, int numPages) {
		super(bytes, contentType);
		this.numPages = OptionalInt.of(numPages);
	}

	public OptionalInt numPages() {
		return numPages;
	}
}
