package com._4point.aem.formspipeline.utils;

import java.time.Duration;
import java.time.Instant;

public class ProcessingMetadataDetails {
	private final int transactionSize;         
    private final Instant startTime;
    private final Instant endTime;
    private final String stepName;                    
    private final String stepDetails;             
    private final String fileCreatedLocation;
    
	public int getTransactionSize() {
		return transactionSize;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public String getStepName() {
		return stepName;
	}

	public String getStepDetails() {
		return stepDetails;
	}

	public String getFileCreatedLocation() {
		return fileCreatedLocation;
	}

	public ProcessingMetadataDetails(int transactionSize, Instant startTime, String stepName, String stepDetails, String fileCreatedLocation) {
		super();
		this.transactionSize = transactionSize;
		this.startTime = startTime;
		this.stepName = stepName;
		this.stepDetails = stepDetails;
		this.fileCreatedLocation = fileCreatedLocation;
		this.endTime = Instant.now();
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
	
	@Override
	public String toString() {
		return "ProcessingMetadata [transactionSize=" + transactionSize + ", startTime=" + startTime + ", endTime=" + endTime+ ", stepName=" + stepName+ 
				", stepDetails=" + stepDetails+ ", fileCreatedLocation=" + fileCreatedLocation +"]";
	}
	
	public static class ProcessingMetadataDetailBuilder {
		private final Instant startTime;
		private int transactionSize;
	    private String stepName;                    
	    private String stepDetails;             
	    private String fileCreatedLocation;
				
		private ProcessingMetadataDetailBuilder() {
			super();
			this.startTime = Instant.now();
		}
		
		private ProcessingMetadataDetailBuilder(int transactionSize, String stepName, String stepDetails) {
			super();
			this.startTime = Instant.now();
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
		
		public ProcessingMetadataDetailBuilder setFileCreatedLocation(String fileCreatedLocation) {
			this.fileCreatedLocation = fileCreatedLocation;
			return this;
		}

		public ProcessingMetadataDetails finish() {
			return new ProcessingMetadataDetails(this.transactionSize, this.startTime, this.stepName, this.stepDetails, this.fileCreatedLocation);
		}

	}
    
    

}
