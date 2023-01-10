package com._4point.aem.formspipeline.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Chunks are generic objects that contain data (i.e. a set of bytes). 
 * 
 */
public interface Chunk {
	/**
	 * Retrieve the data stored in this chunk as an array of bytes.
	 * 
	 * @return
	 */
	public byte[] bytes();

	/**
	 * Retrieve the data stored in this chunk as a String.
	 * 
	 * This method assumes that the bytes stored in this chunk are a UTF-8 string.
	 * 
	 * @return
	 */
	default String asString() { return new String(bytes(), StandardCharsets.UTF_8); }

	/**
	 * Retrieve the data stored in this chunk as an InputStream.
	 * 
	 * @return
	 */
	default InputStream asInputStream() { return new ByteArrayInputStream(bytes()); }

	/**
	 * Retrieve the data stored in this chunk as a Reader.
	 * 
	 * This method assumes that the bytes stored in this chunk are a UTF-8 string.
	 * 
	 * @return
	 */
	default Reader asReader() { return new InputStreamReader(asInputStream(), StandardCharsets.UTF_8);}
}
