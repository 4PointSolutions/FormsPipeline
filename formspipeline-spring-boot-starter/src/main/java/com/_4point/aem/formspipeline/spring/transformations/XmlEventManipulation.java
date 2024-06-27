package com._4point.aem.formspipeline.spring.transformations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

abstract class XmlEventManipulation {
	private static final Logger logger = LoggerFactory.getLogger(XmlEventManipulation.class);
	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
	private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

	protected static List<EventInfo> readToList(XmlDataChunk dataChunk) throws XMLStreamException, IOException {
		try (InputStream os = dataChunk.asInputStream()) {
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(os);
			return EventInfo.readToList(reader);
		}
	}

	protected record TransactionInfo(List<EventInfo> preamble, List<List<EventInfo>> transactions, List<EventInfo> postamble) {
		
		byte[] replayTransactions(int transactionNumber) throws XMLStreamException, IOException {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			// Create ByteArrayOuputStream to capture output, OutputStreamWriter to force UTF-8 output, then XMLEventWriter that is AutoCloseable 
			try (os; var osw = new OutputStreamWriter(os, StandardCharsets.UTF_8); var ew = new AutoCloseableXmlEventWriter(xmlOutputFactory.createXMLEventWriter(osw))) {
				writePreamble(ew);
				writeTransaction(ew, transactionNumber);
				writePostamble(ew);
			}
			return os.toByteArray();
		}
		
		private TransactionInfo writePreamble(XMLEventWriter ew) throws XMLStreamException {
			EventInfo.writeList(ew, preamble);
			return this;
		}

		private TransactionInfo writeTransaction(XMLEventWriter ew, int transactionNumber) throws XMLStreamException {
			List<EventInfo> eventList = transactions.get(transactionNumber);
			logger.atDebug().addArgument(eventList.size()).addArgument(transactionNumber).log("Replaying {} events for transaction #{}.");
			EventInfo.writeList(ew, eventList);
			return this;
		}

		private TransactionInfo writePostamble(XMLEventWriter ew) throws XMLStreamException {
			EventInfo.writeList(ew, postamble);
			return this;
		}

	}

	/**
	 * Reads an XML Data Chunk and creates a list of EventInfo objects.
	 * 
	 * Each EventInfo has an XMLEvent and a depth in the XML tree where it occurs.
	 * 
	 * The depth information is useful later when we determine what is pre/post amble
	 * and what are actualy transactions.
	 *
	 */
	protected record EventInfo(XMLEvent xmlEvent, int depth){
		/**
		 * Read the whole XML in as a list of events so that we can replay them.
		 *  
		 * @param dataChunk
		 * @return List of EventInfo objects
		 * @throws XMLStreamException
		 * @throws IOException
		 */
		protected static List<EventInfo> readToList(XMLEventReader reader) throws XMLStreamException {
			List<EventInfo> eventList = new ArrayList<>(1024);
			int currentLevel = 0;
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				if (event.isStartElement()) { currentLevel++; }
				eventList.add(new EventInfo(event, currentLevel));
				if (event.isEndElement()) { currentLevel--; }
			}
			return eventList;
		}

		protected static void writeList(XMLEventWriter writer, List<EventInfo> eventList) throws XMLStreamException {
			for(EventInfo eventInfo : eventList) {
				writer.add(eventInfo.xmlEvent);
			}
		}
	};

	/**
	 * Stupid boilerplate class because XMLEventWriter does not implement AutoCloseable.
	 *
	 */
	protected static class AutoCloseableXmlEventWriter implements XMLEventWriter, AutoCloseable {
		private final XMLEventWriter xmlEventWriter;

		public AutoCloseableXmlEventWriter(XMLEventWriter xmlEventWriter) {
			this.xmlEventWriter = xmlEventWriter;
		}

		public void flush() throws XMLStreamException {
			xmlEventWriter.flush();
		}

		public void close() throws XMLStreamException {
			xmlEventWriter.close();
		}

		public void add(XMLEvent event) throws XMLStreamException {
			xmlEventWriter.add(event);
		}

		public void add(XMLEventReader reader) throws XMLStreamException {
			xmlEventWriter.add(reader);
		}

		public String getPrefix(String uri) throws XMLStreamException {
			return xmlEventWriter.getPrefix(uri);
		}

		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			xmlEventWriter.setPrefix(prefix, uri);
		}

		public void setDefaultNamespace(String uri) throws XMLStreamException {
			xmlEventWriter.setDefaultNamespace(uri);
		}

		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			xmlEventWriter.setNamespaceContext(context);
		}

		public NamespaceContext getNamespaceContext() {
			return xmlEventWriter.getNamespaceContext();
		}
	}
}
