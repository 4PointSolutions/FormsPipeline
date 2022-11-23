package com._4point.aem.formspipeline.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public interface Chunk {
	public byte[] bytes();
	default String asString() { return new String(bytes(), StandardCharsets.UTF_8); }
	default InputStream asInputStream() { return new ByteArrayInputStream(bytes()); }
	default Reader asReader() { return new InputStreamReader(asInputStream(), StandardCharsets.UTF_8);}
}
