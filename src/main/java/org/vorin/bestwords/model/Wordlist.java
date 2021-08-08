package org.vorin.bestwords.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.LogFactory;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.AppConfig.RES_DIR;

@XmlRootElement(name = "wordList")
public class Wordlist {

    private static final Logger LOG = Logger.get(Wordlist.class);

    private static final Map<String, Translation> originalWordlist = new HashMap<>();

    static {
        try {
            var wl = loadFromXml(new File(RES_DIR + "EnglishWordlist1k.xml"));
            wl.getTranslations().stream().forEach(t -> {
               originalWordlist.put(t.foreignWord, t);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private List<Translation> translations;


    public Wordlist() {
        this.translations = new ArrayList<>();
    }


    public static Wordlist loadFromXml(File xmlFile) throws IOException {
        try (var xmlFileIS = new FileInputStream(xmlFile)) {
            return JAXB.unmarshal(xmlFileIS, Wordlist.class);
        }
    }


    public static Wordlist loadFromTxtList(File file) throws IOException {
        List<String> words = Util.loadWordsFromTxtFile2(file);

        var translations = words.stream().map(word -> new Translation(word, "", "", new ArrayList<>())).collect(toList());
        var wordlist = new Wordlist();
        wordlist.setTranslations(translations);
        return wordlist;
    }


    public void writeToXml(File xmlFile) throws IOException {
        try (var xmlFileOS = new FileOutputStream(xmlFile)) {
            JAXB.marshal(this, xmlFileOS);
        }
    }


    public Meaning addMeaning(String foreignWord, String wordMeaning, String wordType, String wordMeaningSource) {
        checkArgument(!isNullOrEmpty(foreignWord) &&
                !isNullOrEmpty(wordMeaning) &&
                !isNullOrEmpty(wordMeaningSource));

        var translation = findTranslationForWord(foreignWord);
        if (translation == null) {
            translation = new Translation(foreignWord,
                                          getPronunciation(foreignWord),
                                          getAudioName(foreignWord),
                                          new ArrayList<>());
            translations.add(translation);
        }

        if (findMeaning(translation, wordMeaning, wordType) != null) {
//            throw new RuntimeException(format("The translation: foreignWord [%s] and wordMeaning [%s] and wordType [%s] already exists",
//                                              foreignWord, wordMeaning, wordType));
            LOG.warn(format("The translation: foreignWord [%s] and wordMeaning [%s] and wordType [%s] already exists",
                    foreignWord, wordMeaning, wordType));

        }

        translation.getMeanings().add(new Meaning(wordMeaning, wordType, null, null, wordMeaningSource, null));
        return translation.getMeanings().get(translation.getMeanings().size() - 1);
    }


    public void addExampleSentence(String foreignWord,
                                   String wordMeaning,
                                   String wordType,
                                   String exampleSentence,
                                   String exampleSentenceSource) {
        checkArgument(!isNullOrEmpty(foreignWord) &&
                !isNullOrEmpty(wordMeaning) &&
                !isNullOrEmpty(wordType) &&
                !isNullOrEmpty(exampleSentence) &&
                !isNullOrEmpty(exampleSentenceSource));

        var translation = findTranslationForWord(foreignWord);
        if (translation == null) {
            throw new RuntimeException(format("Translation for foreignWord=[%s] does not exist", foreignWord));
        }

        var meaning = findMeaning(translation, wordMeaning, wordType);
        if (meaning == null) {
            throw new RuntimeException(format("Meaning for foreignWord=[%s] and wordMeaning=[%s] and wordType=[%s] does not exist", foreignWord, wordMeaning, wordType));
        }

        meaning.setExampleSentence(exampleSentence);
        meaning.setExampleSentenceSource(exampleSentenceSource);
    }


    public Translation findTranslationForWord(String foreignWord) {
        var translations = this.translations.stream().filter(t -> t.getForeignWord().equals(foreignWord)).collect(toList());
        if (translations.isEmpty()) {
            return null;
        }
        if (translations.size() > 1) {
            throw new RuntimeException(format("There is more than one translation with foreignWord [%s]", foreignWord));
        }

        return translations.get(0);
    }


    public Meaning findMeaning(String foreignWord, String wordMeaning, String wordType) {
        var t = findTranslationForWord(foreignWord);
        if (t == null) {
            return null;
        }
        return findMeaning(t, wordMeaning, wordType);
    }


    public static Meaning findMeaning(Translation translation, String wordMeaning, String wordType) {
        var meanings = translation.getMeanings().stream()
                .filter(m -> m.getWordMeaning().equals(wordMeaning) && m.getWordType().equals(wordType))
                .collect(toList());
        if (meanings.isEmpty()) {
            return null;
        }
        if (meanings.size() > 1) {
//            throw new RuntimeException(format("There is more than one wordMeaning [%s] for translation [%s] and wordType[%s]", wordMeaning, translation, wordType));
            LOG.warn(format("There is more than one wordMeaning [%s] and for translation [%s] and wordType[%s]", wordMeaning, translation, wordType));
        }

        return meanings.get(0);
    }


    public List<Translation> getTranslations() {
        return translations;
    }


    @XmlElement(name = "translation")
    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }


    public int size() {
        return getTranslations().size();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Wordlist wordlist = (Wordlist) o;

        return new EqualsBuilder()
                .append(translations, wordlist.translations)
                .isEquals();
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(translations)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("translations", translations)
                .toString();
    }


    private String getAudioName(String foreignWord) {
        var t = originalWordlist.get(foreignWord);
        if (t == null) {
            return "";
        }
        return originalWordlist.get(foreignWord).getAudioName();
    }


    private String getPronunciation(String foreignWord) {
        var t = originalWordlist.get(foreignWord);
        if (t == null) {
            return "";
        }
        return originalWordlist.get(foreignWord).getPronunciation();
    }

}
