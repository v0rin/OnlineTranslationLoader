package org.vorin.bestwords;

import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Logger;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

public class XmlTranslationPublisher implements TranslationPublisher {

	private static final Logger LOG = Logger.get(XmlTranslationPublisher.class);

	private final File xmlFile;
	private final WordList wordlist;

	public XmlTranslationPublisher(File xmlFile) {
		this.xmlFile = xmlFile;
		this.wordlist = new WordList();
	}

	@Override
	public void addTranslation(String foreignWord,
							   String wordMeaning,
							   String exampleForeignSentence,
							   String exampleTranslatedSentence) {
		LOG.info(format("added translation: foreightWord=%s, meaning=%s, exampleForeignSentence=%s, exampleTranslatedSentence=%s",
				foreignWord, wordMeaning, exampleForeignSentence, exampleTranslatedSentence));

		wordlist.addTranslation(foreignWord, wordMeaning, exampleForeignSentence, exampleTranslatedSentence);
	}

	@Override
	public void writeToTarget() throws IOException {
		wordlist.writeToXml(xmlFile);
	}

}