package com._4point.aem.formspipeline.spring.transformations;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;


import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com._4point.aem.formspipeline.spring.transformations.XmlEventManipulation.EventInfo;
import com._4point.aem.formspipeline.spring.transformations.XmlEventManipulation.TransactionInfo;

class XmlEventManipulationTest {
	private static final int EXPECTED_NUM_EVENTS = 65;
	private static final String STATEMENT1 =
			"""
		   <statement account="123">
		      ...stuff...
		      <foo>bar</foo>
		      <emptyTag />
		      ...stuff...
		   </statement>""";
	private static final String STATEMENT2 =
			"""
		   <statement account="456">
		      <nested1>
			     <nested2>
			        <nested3_1>Some Data</nested3_1>
			        <nested3_2/>
			        <nested3_3>Some More Data with Unicode character ℃</nested3_3><nested3_4></nested3_4>
			     </nested2>
		      </nested1>
		      ...stuff...
		   </statement>
		   """;
	private static final String STATEMENT3 =
			"""
		   <statement account="789">
		      <nested1>
			     <nested2>
			        <nested3_1>Some Data</nested3_1>
			        <nested3_2/>
			        <nested3_3>Some More Data</nested3_3><nested3_4></nested3_4>
			     </nested2>
		      </nested1>
		      ...stuff...
		   </statement>""";
	private static final String XML_WRAPPER_FMT_STRING = 
			"<output><statements>%s</statements></output>\n";

	static final byte[] FULL_TRANSACTION = XML_WRAPPER_FMT_STRING.formatted(STATEMENT1 + STATEMENT2 + STATEMENT3).getBytes(StandardCharsets.UTF_8);

	
	static final byte[] TRANSACTION_1 = XML_WRAPPER_FMT_STRING.formatted(STATEMENT1).getBytes(StandardCharsets.UTF_8);
	static final byte[] TRANSACTION_2 = XML_WRAPPER_FMT_STRING.formatted(STATEMENT2).getBytes(StandardCharsets.UTF_8);
	static final byte[] TRANSACTION_3 = XML_WRAPPER_FMT_STRING.formatted(STATEMENT3).getBytes(StandardCharsets.UTF_8);

	
	@Test
	void testReadToList() throws Exception {
		List<EventInfo> result = XmlEventManipulation.readToList(XmlDataChunk.create(XmlEventManipulationTest.FULL_TRANSACTION));
		
//		printEventList(result);
		assertEquals(result.size(), EXPECTED_NUM_EVENTS, ()->"Expected " + EXPECTED_NUM_EVENTS + " events in the sample XML.");
	}

	@Test
	void testConvertToTransactions() throws Exception {
		List<EventInfo> input = XmlEventManipulation.readToList(XmlDataChunk.create(XmlEventManipulationTest.FULL_TRANSACTION));
		TransactionInfo result = XmlEventManipulation.convertToTransactions(input, 2);
		
//		System.out.println("------ PreAmble ");
//		printEventList(result.preamble());
//		System.out.println("------ PostAmble");
//		printEventList(result.postamble());
//		System.out.println("------ Transactions");
//		result.transactions().stream().forEach(l->{
//			System.out.println("------ Transaction");
//			System.out.println("Found " + l.size() + " events in transaction.");	
//			printEventList(l);
//		});
//		System.out.println("------");
		
		var numPreambleEvents = result.preamble().size();
		var numPostAmbleEvents = result.postamble().size();
		var numTransactionEvents = result.transactions().stream().mapToInt(List::size).sum();
		
//		result.transactions().stream().forEach(l->System.out.println("Found " + l.size() + " events in transaction."));
		
		assertEquals(input.size() - 1, numPreambleEvents + numPostAmbleEvents + numTransactionEvents, ()->"Expected same number of events minus 1 (-1 because text between transactions is omitted) but had " + numPreambleEvents + " preamble, " + numPostAmbleEvents + " postamble, and " + numTransactionEvents + " transactioin events");
		
	}

	// Helpful debugging routine.
//	private void printEventList(List<EventInfo> result) {
//		result.forEach(e->System.out.println("event = " + e.xmlEvent().toString() + " depth=" + e.depth() ));
//	}
}
