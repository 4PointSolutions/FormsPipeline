package com._4point.aem.formspipeline.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.utils.ProcessingMetadataDetails.ProcessingMetadataDetailBuilder;

class ProcessingMetadataDetailsTest {
	Instant now = Instant.now();
	private static final int SLEEP_TIME = 50;
	private static final int TEST_TRANSACTION_SIZE = 23;
	private static final String STEP_NAME = "UnitTest";
	private static final String STEP_DETAILS = "UnitTestDetails";
	private static final String FILE_LOCATION = "C:\\Temp";
			
	@Test
	void test_timeCalculation() throws InterruptedException {
		Instant testStart = Instant.now();
		Thread.sleep(SLEEP_TIME);
		ProcessingMetadataDetailBuilder metadataBuilder = ProcessingMetadataDetails.start();
		Thread.sleep(SLEEP_TIME);
		metadataBuilder.setTransactionSize(TEST_TRANSACTION_SIZE);
		metadataBuilder.setStepName(STEP_NAME);
		metadataBuilder.setStepDetails(STEP_DETAILS);
		metadataBuilder.setFileCreatedLocation(FILE_LOCATION);
	
		ProcessingMetadataDetails result = metadataBuilder.finish();
		Thread.sleep(SLEEP_TIME);
		Instant testEnd = Instant.now();

		System.out.println(result.getFormattedElapsedTime());
		
		assertAll(
				()->assertTrue(result.getElapsedTimeMs() >= SLEEP_TIME),
				()->assertEquals(TEST_TRANSACTION_SIZE, result.getTransactionSize()),
				()->assertEquals(STEP_NAME, result.getStepName()),
				()->assertEquals(STEP_DETAILS, result.getStepDetails()),
				()->assertEquals(FILE_LOCATION, result.getFileCreatedLocation()),
				()->assertTrue(testStart.isBefore(result.getStartTime())),
				()->assertTrue(testEnd.isAfter(result.getStartTime())),
				()->assertTrue(testStart.isBefore(result.getEndTime())),
				()->assertTrue(testEnd.isAfter(result.getEndTime())),
				()->assertFalse(result.getFormattedElapsedTime().isEmpty() || result.getFormattedElapsedTime().trim().isEmpty())
				);
	}
	
	@Test
	void test_toString() throws InterruptedException{
		String EXPECTED_TRANSACTION = "ProcessingMetadata [transactionSize=" + TEST_TRANSACTION_SIZE;		
		String EXPECTED_START_TIME = ", startTime=" + now;
		String EXPECTED_STEP_NAME = ", stepName=" + STEP_NAME;
		String EXPECTED_STEP_DETAILS = ", stepDetails=" + STEP_DETAILS;
		String EXPECTED_FILE_LOCATION = ", fileCreatedLocation=" + FILE_LOCATION +"]";
		
		Instant testStart = Instant.now();	
		Thread.sleep(SLEEP_TIME);
		ProcessingMetadataDetails underTest = new ProcessingMetadataDetails(TEST_TRANSACTION_SIZE, now, STEP_NAME, STEP_DETAILS,FILE_LOCATION);
		Thread.sleep(SLEEP_TIME);
		Instant testEnd = Instant.now();
		
		System.out.println(underTest.toString());
		assertTrue(underTest.toString().contains(EXPECTED_TRANSACTION));
		assertTrue(underTest.toString().contains(EXPECTED_START_TIME));
		assertTrue(underTest.toString().contains(EXPECTED_STEP_NAME));
		assertTrue(underTest.toString().contains(EXPECTED_STEP_DETAILS));
		assertTrue(underTest.toString().contains(EXPECTED_FILE_LOCATION));	
		assertTrue(testEnd.isAfter(underTest.getStartTime()));
		assertTrue(testStart.isBefore(underTest.getEndTime()));
		
	}

}
