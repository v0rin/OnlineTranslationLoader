package org.vorin.bestwords;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

public class XmlTranslationPublisher implements TranslationPublisher {

	private static final Logger LOG = Logger.get(XmlTranslationPublisher.class);

	private File xmlFile;

	public XmlTranslationPublisher(File xmlFile) {
		this.xmlFile = xmlFile;
	}

	@Override
	public void addTranslation(String foreignWord, String meaning, String exampleForeignSentence, String exampleTranslatedSentence) {
		LOG.info(format("added translation: foreightWord=%s, meaning=%s, exampleForeignSentence=%s, exampleTranslatedSentence=%s",
				foreignWord, meaning, exampleForeignSentence, exampleTranslatedSentence));
	}

	@Override
	public void writeToTarget() throws IOException {
		// TODO @af write to the xml file
	}

}