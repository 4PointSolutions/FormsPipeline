package com._4point.aem.formspipeline.chunks;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class SimpleChunkTest {

	private static final String TEST_STRING = "Test Data";
	private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);
	
	private final SimpleChunk underTest_1 = new SimpleChunk(TEST_BYTES);
	
	@Test
	void testBytes_1() {
		assertArrayEquals(TEST_BYTES, underTest_1.bytes());
	}

	@Test
	void testAsString_1() {
		assertEquals(TEST_STRING, underTest_1.asString());
	}

	@Test
	void testAsInputStream_1() throws Exception {
		assertArrayEquals(TEST_BYTES, underTest_1.asInputStream().readAllBytes());
	}

	@Test
	void testAsReader_1() throws Exception {
		assertEquals(TEST_STRING, new BufferedReader(underTest_1.asReader()).readLine());
	}

	private final SimpleChunk underTest_2 = new SimpleChunk(TEST_STRING);
	
	@Test
	void testBytes_2() {
		assertArrayEquals(TEST_BYTES, underTest_2.bytes());
	}

	@Test
	void testAsString_2() {
		assertEquals(TEST_STRING, underTest_2.asString());
	}

	@Test
	void testAsInputStream_2() throws Exception {
		assertArrayEquals(TEST_BYTES, underTest_2.asInputStream().readAllBytes());
	}

	@Test
	void testAsReader_2() throws Exception {
		assertEquals(TEST_STRING, new BufferedReader(underTest_2.asReader()).readLine());
	}

}
