package com._4point.aem.formspipeline.utils;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Contains the processing details for a step in the forms pipeline. 
 *
 */
public record ProcessingMetadataDetails(int transactionSize, Instant startTime, Instant endTime, String stepName, String stepDetails, Path fileCreatedLocation) {

	/**
	 * Constructor that assumes that the endTime is Instant.now().
	 * 
	 * @param transactionSize
	 * @param startTime
	 * @param stepName
	 * @param stepDetails
	 * @param fileCreatedLocation
	 */
	public ProcessingMetadataDetails(int transactionSize, Instant startTime, String stepName, String stepDetails, Path fileCreatedLocation) {
		// Assumes the endTime is now.
		this(transactionSize, startTime, Instant.now(), stepName, stepDetails, fileCreatedLocation);
	}
	
	public long getElapsedTimeMs() {
		return Duration.between(startTime, endTime).toMillis();
	}
	
	public String getFormattedElapsedTime() {
		long elapsedTimeMs = getElapsedTimeMs();
		long second = (elapsedTimeMs / 1000) % 60;
		long minute = (elapsedTimeMs / (1000 * 60)) % 60;
		long hour   = (elapsedTimeMs / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d.%03d", hour, minute, second, (elapsedTimeMs % 1000));
	}

	public static ProcessingMetadataDetailBuilder start(int transactionSize, String stepName, String stepDetails) {
		return new ProcessingMetadataDetailBuilder(transactionSize, stepName, stepDetails);
	}
	
	public static class ProcessingMetadataDetailBuilder {
		private final Instant startTime;
		private int transactionSize;
	    private String stepName;                    
	    private String stepDetails;             
	    private Path fileCreatedLocation;
				
		private ProcessingMetadataDetailBuilder() {
			this.startTime = Instant.now();
		}
		
		private ProcessingMetadataDetailBuilder(int transactionSize, String stepName, String stepDetails) {
			this();
			this.transactionSize = transactionSize;
			this.stepName = stepName;
			this.stepDetails = stepDetails;
		}

		public ProcessingMetadataDetailBuilder setTransactionSize(int transactionSize) {
			this.transactionSize = transactionSize;
			return this;
		}
		
		public ProcessingMetadataDetailBuilder setStepName(String stepName) {
			this.stepName = stepName;
			return this;
		}
		
		public ProcessingMetadataDetailBuilder setStepDetails(String stepDetails) {
			this.stepDetails = stepDetails;
			return this;
		}
		
		public ProcessingMetadataDetailBuilder setFileCreatedLocation(Path fileCreatedLocation) {
			this.fileCreatedLocation = fileCreatedLocation;
			return this;
		}

		public ProcessingMetadataDetails finish() {
			return new ProcessingMetadataDetails(this.transactionSize, this.startTime, this.stepName, this.stepDetails, this.fileCreatedLocation);
		}
	}
}
