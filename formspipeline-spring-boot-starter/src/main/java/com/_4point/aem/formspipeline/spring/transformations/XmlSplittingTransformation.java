package com._4point.aem.formspipeline.spring.transformations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToMany;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

/**
 * Splits an XmlChunk into multiple XmlChunks.
 * 
 * Assumes that the root level will be discarded and one XmlChunk will be
 * created per element below the root.
 * 
 */
public class XmlSplittingTransformation implements DataTransformationOneToMany<XmlDataChunk, XmlDataChunk> {
	private static final Logger logger = LoggerFactory.getLogger(XmlSplittingTransformation.class);

	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
	private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

	@Override
	public Stream<XmlDataChunk> process(XmlDataChunk dataChunk) {
		try {
			XMLEventReader xsr = xmlInputFactory.createXMLEventReader(dataChunk.asInputStream());
			var event = xsr.nextTag(); // Advance to second level element
			event = xsr.nextTag(); // Advance to second level element

			Builder<XmlDataChunk> streamBuilder = Stream.builder();
			while (event.isStartElement()) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				XMLEventWriter ew = xmlOutputFactory.createXMLEventWriter(os);
				event = copyTag(event, xsr, ew);
				os.close();
				streamBuilder.accept(XmlDataChunk.create(os.toByteArray(), dataChunk.dataContext()));
				while(event.isCharacters()) {	
					event = xsr.nextEvent();	// Eat all the characters until the next 
				}
			}
			return streamBuilder.build();
		} catch (XMLStreamException | TransformerFactoryConfigurationError | IOException e) {
			throw new IllegalStateException("Failed to split XML file.  %s".formatted(e.getMessage()), e);
		}
	}

	XMLEvent copyTag(XMLEvent currentEvent, XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
		String currentTagName = currentEvent.asStartElement().getName().getLocalPart();
		logger.atTrace().log("Copying Start Tag {}", currentTagName);
		writer.add(currentEvent);
		for(currentEvent = reader.nextEvent();!currentEvent.isEndElement();currentEvent = reader.nextEvent()) {
			if (currentEvent.isStartElement()) {
				currentEvent = copyTag(currentEvent, reader, writer);
			}
			writer.add(currentEvent);
		}
		logger.atTrace().log("Copying End Tag {}", currentTagName);
		writer.add(currentEvent);	// Write out the endElement event

		return reader.nextEvent();
	}
}
