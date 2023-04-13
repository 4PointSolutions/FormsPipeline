package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.spring.chunks.JsonDataChunk;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

class JsonToXmlDataTransformationTest {

	private static final String SAMPLE_JSON =
			"""
			{
			    "accountType": "d",
			    "ownerType": "mqacR",
			    "primaryClientRpoFlag": false,
			    "dealerReps": {
			        "dealerCode": "hASCxJzPfLCZORiaGL",
			        "phone": {
			            "mobilePhoneNumber": 26,
			            "otherPhoneNumber": 66,
			            "preferredPhone": "other"
			        },
			        "esignatureAxisFlag": true
			    },
			    "advisor": {
			        "drCode": "qIdwzkXNPcPkUSrQY",
			        "company": "AcqdLz",
			        "advisor": "HAEPKQUUYhUCzRFU",
			        "phone": {
			            "mobilePhoneNumber": 32,
			            "otherPhoneNumber": 49,
			            "preferredPhone": "mobile"
			        }
			    },
			    "account": {
			        "whoToSign": "oAo",
			        "inTrustFor": "uazkF",
			        "sibling": "IZotyMWt",
			        "crossref": "zfbfNh",
			        "province": "Saskatchewan"
			    }
			}
			""";
	
	private static final String EXPECTED_XML =
			"""
			<?xml version="1.0" encoding="UTF-8"?>
			<JSON>
			    <accountType>d</accountType>
			    <ownerType>mqacR</ownerType>
			    <primaryClientRpoFlag>false</primaryClientRpoFlag>
			    <dealerReps>
			        <dealerCode>hASCxJzPfLCZORiaGL</dealerCode>
			        <phone>
			            <mobilePhoneNumber>26</mobilePhoneNumber>
			            <otherPhoneNumber>66</otherPhoneNumber>
			            <preferredPhone>other</preferredPhone>
			        </phone>
			        <esignatureAxisFlag>true</esignatureAxisFlag>
			    </dealerReps>
			    <advisor>
			        <drCode>qIdwzkXNPcPkUSrQY</drCode>
			        <company>AcqdLz</company>
			        <advisor>HAEPKQUUYhUCzRFU</advisor>
			        <phone>
			            <mobilePhoneNumber>32</mobilePhoneNumber>
			            <otherPhoneNumber>49</otherPhoneNumber>
			            <preferredPhone>mobile</preferredPhone>
			        </phone>
			    </advisor>
			    <account>
			        <whoToSign>oAo</whoToSign>
			        <inTrustFor>uazkF</inTrustFor>
			        <sibling>IZotyMWt</sibling>
			        <crossref>zfbfNh</crossref>
			        <province>Saskatchewan</province>
			    </account>
			</JSON>
			""";

	private final JsonToXmlDataTransformation underTest = new JsonToXmlDataTransformation("JSON");
	
	@Test
	void testProcess() {
		XmlDataChunk result = underTest.process(JsonDataChunk.create(SAMPLE_JSON.getBytes(StandardCharsets.UTF_8)));
		
		assertThat(Input.fromByteArray(result.bytes()), isIdenticalTo(Input.fromString(EXPECTED_XML)).ignoreWhitespace());
	}

}
