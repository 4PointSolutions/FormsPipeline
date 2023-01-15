package com._4point.aem.formspipeline.utils;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.Instant;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.utils.ProcessingMetadataDetails.ProcessingMetadataDetailBuilder;

class ProcessingMetadataDetailsTest {
	Instant now = Instant.now();
	private static final long SLEEP_TIME = 50;
	private static final int TEST_TRANSACTION_SIZE = 23;
	private static final String STEP_NAME = "UnitTest";
	private static final String STEP_DETAILS = "UnitTestDetails";
	private static final Path FILE_LOCATION = Path.of("Temp");

	private static Matcher<Instant> isAfter(Instant i) { return Matchers.greaterThan(i); }
	private static Matcher<Instant> isBefore(Instant i) { return Matchers.lessThan(i); }
	
	@Test
	void test_timeCalculation() throws InterruptedException {
		Instant testStart = Instant.now();
		Thread.sleep(SLEEP_TIME);
		ProcessingMetadataDetailBuilder metadataBuilder = ProcessingMetadataDetails.start(TEST_TRANSACTION_SIZE,STEP_NAME,STEP_DETAILS);
		Thread.sleep(SLEEP_TIME);
		metadataBuilder.setFileCreatedLocation(FILE_LOCATION);
	
		ProcessingMetadataDetails result = metadataBuilder.finish();
		Thread.sleep(SLEEP_TIME);
		Instant testEnd = Instant.now();

		assertAll(
				()->assertThat(result.getElapsedTimeMs(), greaterThanOrEqualTo(SLEEP_TIME)),
				()->assertEquals(TEST_TRANSACTION_SIZE, result.transactionSize()),
				()->assertEquals(STEP_NAME, result.stepName()),
				()->assertEquals(STEP_DETAILS, result.stepDetails()),
				()->assertEquals(FILE_LOCATION, result.fileCreatedLocation()),
				()->assertThat(testEnd, isAfter(result.startTime())),
				()->assertThat(testStart, isBefore(result.endTime())),
				()->assertThat(testStart, isBefore(result.startTime())),
				()->assertThat(testEnd, isAfter(result.startTime())),
				()->assertThat(testStart, isBefore(result.endTime())),
				()->assertThat(testEnd, isAfter(result.endTime())),
				()->assertThat(result.getFormattedElapsedTime().trim(), not(emptyString()))
				);
	}
	
	@Test
	void test_toString() throws InterruptedException{
		String EXPECTED_TRANSACTION = "ProcessingMetadataDetails[transactionSize=" + TEST_TRANSACTION_SIZE;		
		String EXPECTED_START_TIME = ", startTime=" + now;
		String EXPECTED_STEP_NAME = ", stepName=" + STEP_NAME;
		String EXPECTED_STEP_DETAILS = ", stepDetails=" + STEP_DETAILS;
		String EXPECTED_FILE_LOCATION = ", fileCreatedLocation=" + FILE_LOCATION +"]";
		
		Instant testStart = Instant.now();	
		Thread.sleep(SLEEP_TIME);
		ProcessingMetadataDetails underTest = new ProcessingMetadataDetails(TEST_TRANSACTION_SIZE, now, STEP_NAME, STEP_DETAILS,FILE_LOCATION);
		Thread.sleep(SLEEP_TIME);
		Instant testEnd = Instant.now();

		assertAll(
				()->assertThat(underTest.toString(), containsString(EXPECTED_TRANSACTION)),
				()->assertThat(underTest.toString(), containsString(EXPECTED_START_TIME)),
				()->assertThat(underTest.toString(), containsString(EXPECTED_STEP_NAME)),
				()->assertThat(underTest.toString(), containsString(EXPECTED_STEP_DETAILS)),
				()->assertThat(underTest.toString(), containsString(EXPECTED_FILE_LOCATION)),
				()->assertThat(testEnd, isAfter(underTest.startTime())),
				()->assertThat(testStart, isBefore(underTest.endTime()))
				);
	}
}
