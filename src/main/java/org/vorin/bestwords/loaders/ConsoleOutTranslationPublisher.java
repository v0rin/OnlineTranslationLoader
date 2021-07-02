package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Logger;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

public class ConsoleOutTranslationPublisher implements TranslationPublisher {

	private static final Logger LOG = Logger.get(ConsoleOutTranslationPublisher.class);

	private final WordList wordlist;

	public ConsoleOutTranslationPublisher() {
		this.wordlist = new WordList();
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
	public WordList getWordList() {
		return wordlist;
	}

	@Override
	public void writeToTarget() {
		wordlist.getTranslations().forEach(System.out::println);
	}

}