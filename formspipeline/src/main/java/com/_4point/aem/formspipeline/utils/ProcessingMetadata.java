package com._4point.aem.formspipeline.utils;

import java.util.ArrayList;
import java.util.List;

public class ProcessingMetadata {
	private final String correlationId;
	private List<ProcessingMetadataDetails> dataDetails = new ArrayList<ProcessingMetadataDetails>();
	
	public ProcessingMetadata(String correlationId){
		super();
		this.correlationId = correlationId;
	}
	
	public void addProcessingMetadataDetails(ProcessingMetadataDetails processData) {
		this.dataDetails.add(processData);
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public List<ProcessingMetadataDetails> getDataDetails() {
		return dataDetails;
	}
}
