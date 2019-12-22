package org.vorin.bestwords.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
public class WordList {

    private static final Map<String, Translation> originalWordList = new HashMap<>();

    static {
        try {
            var wl = loadFromXml(new File(RES_DIR + "EnglishWordList1k.xml"));
            wl.getTranslations().stream().forEach(t -> {
               originalWordList.put(t.foreignWord, t);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private List<Translation> translations;


    public WordList() {
        this.translations = new ArrayList<>();
    }


    public static WordList loadFromXml(File xmlFile) throws IOException {
        return JAXB.unmarshal(new FileInputStream(xmlFile), WordList.class);
    }


    public void writeToXml(File xmlFile) throws IOException {
        JAXB.marshal(this, new FileOutputStream(xmlFile));
    }


    public void addMeaning(String foreignWord, String wordMeaning, String source) {
        checkArgument(!isNullOrEmpty(foreignWord) &&
                !isNullOrEmpty(wordMeaning) &&
                !isNullOrEmpty(source));

        var translation = findTranslationForWord(foreignWord);
        if (translation == null) {
            translation = new Translation(foreignWord,
                                          getPronunciation(foreignWord),
                                          getAudioName(foreignWord),
                                          new ArrayList<>());
            translations.add(translation);
        }

        translation.getMeanings().add(new Meaning(wordMeaning, null, null, source, null));
    }


    public void addExampleSentence(String foreignWord,
                                   String wordMeaning,
                                   String exampleSentence,
                                   String source) {
        checkArgument(!isNullOrEmpty(foreignWord) &&
                !isNullOrEmpty(wordMeaning) &&
                !isNullOrEmpty(exampleSentence) &&
                !isNullOrEmpty(source));

        var translation = findTranslationForWord(foreignWord);
        if (translation == null) {
            throw new RuntimeException(format("Translation for foreignWord=[%s] does not exist", foreignWord));
        }

        var meaning = findMeaning(translation, wordMeaning);
        if (meaning == null) {
            throw new RuntimeException(format("Meaning for foreignWord=[%s] and wordMeaning=[%s] does not exist", foreignWord, wordMeaning));
        }

        meaning.setExampleSentence(exampleSentence);
        meaning.setExampleSentenceSource(source);
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


    public Meaning findMeaning(Translation translation, String wordMeaning) {
        var meanings = translation.getMeanings().stream().filter(m -> m.getWordMeaning().equals(wordMeaning)).collect(toList());
        if (meanings.isEmpty()) {
            return null;
        }
        if (meanings.size() > 1) {
            throw new RuntimeException(format("There is more than one wordMeaning [%s] for translation [%s]", wordMeaning, translation));
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WordList wordList = (WordList) o;

        return new EqualsBuilder()
                .append(translations, wordList.translations)
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
        return originalWordList.get(foreignWord).getAudioName();
    }


    private String getPronunciation(String foreignWord) {
        return originalWordList.get(foreignWord).getPronunciation();
    }

}
