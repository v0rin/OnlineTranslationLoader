package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

public class XmlTranslationPublisher implements TranslationPublisher {

	private static final Logger LOG = Logger.get(XmlTranslationPublisher.class);

	private final File xmlFile;
	private final Wordlist wordlist;

	public XmlTranslationPublisher(File xmlFile) {
		this.xmlFile = xmlFile;
		this.wordlist = new Wordlist();
	}

	@Override
	public void addMeaning(String foreignWord, String meaning, String source) {
		addMeaning(foreignWord, meaning, null, source, null);
	}

	@Override
	public void addMeaning(String foreignWord, String meaning, String wordType, String source) {
		addMeaning(foreignWord, meaning, wordType, source, null);
	}

	@Override
	public void addMeaning(String foreignWord, String wordMeaning, String wordType, String source, String comment) {
		var meaning = wordlist.addMeaning(foreignWord, wordMeaning, wordType, source);
		meaning.addComment(comment);
		LOG.info(format("added meaning from [%s]: foreignWord=%s, meaning=%s, wordType=%s, comment=%s, total words=%s",
				source, foreignWord, wordMeaning, wordType, comment, wordlist.size()));
	}

	@Override
	public void addExampleSentence(String foreignWord,
								   String wordMeaning,
								   String exampleSentence,
								   String source) {
		LOG.info(format("added example sentence from [%s]: foreignWord=%s, meaning=%s, exampleSentence=%s",
						source, foreignWord, wordMeaning, exampleSentence));
		wordlist.addExampleSentence(foreignWord, wordMeaning, exampleSentence, source);
	}

	@Override
	public boolean exampleSentenceExists(String foreignWord, String wordMeaning) {
		var meaning = wordlist.findMeaning(foreignWord, wordMeaning);

		if (meaning != null && !isNullOrEmpty(meaning.getExampleSentence())) {
			return true;
		}
		return false;
	}

	@Override
	public Wordlist getWordlist() {
		return wordlist;
	}

	@Override
	public void writeToTarget() throws IOException {
		wordlist.writeToXml(xmlFile);
	}

}