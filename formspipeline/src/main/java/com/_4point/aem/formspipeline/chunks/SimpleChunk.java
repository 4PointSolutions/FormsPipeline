package com._4point.aem.formspipeline.chunks;

import java.nio.charset.StandardCharsets;

import com._4point.aem.formspipeline.api.RawData;

/**
 * Simple (i.e. minimal) implementation of a Chunk.
 *
 */
public record SimpleChunk(byte[] bytes) implements RawData {
	/**
	 * Constructor that stores a String's bytes.
	 * 
	 * @param dataString
	 */
	public SimpleChunk(String dataString) { this(dataString.getBytes(StandardCharsets.UTF_8)); }
}
