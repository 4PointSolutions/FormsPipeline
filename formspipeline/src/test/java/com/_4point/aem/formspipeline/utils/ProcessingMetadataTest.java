package com._4point.aem.formspipeline.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ProcessingMetadataTest {
	Instant now = Instant.now();
	
	ProcessingMetadata underTest = new ProcessingMetadata("dummyCorrelationId");
	ProcessingMetadataDetails pmd = new ProcessingMetadataDetails(2, now, "UnitTest", "UnitTestDetails", null);

	@Test
	void test_validateCorrelationId() {
		assertEquals("dummyCorrelationId", underTest.getCorrelationId());
	}
	
	@Test
	void test_validateProcessingMetadata_add() {
		underTest.addProcessingMetadataDetails(pmd);
		assertEquals(pmd,underTest.getDataDetails().get(0));
		assertEquals("dummyCorrelationId", underTest.getCorrelationId());
	}

}
