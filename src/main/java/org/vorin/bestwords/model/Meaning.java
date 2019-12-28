package org.vorin.bestwords.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

@XmlType(propOrder={"wordMeaning","exampleSentence","imgName", "wordMeaningSource", "exampleSentenceSource", "comment"})
public class Meaning {

    private String wordMeaning;
    private String exampleSentence;
    private String imgName;
    private String exampleSentenceSource;
    private String wordMeaningSource;
    private String comment = "";


    public Meaning() {} // needed for JAXB unmarshalling

    public Meaning(String wordMeaning,
                   String exampleSentence,
                   String imgName,
                   String wordMeaningSource,
                   String exampleSentenceSource) {
        checkNotNull(wordMeaning);
        this.wordMeaning = wordMeaning;
        this.exampleSentence = emptyIfNull(exampleSentence);
        this.imgName = emptyIfNull(imgName);
        this.wordMeaningSource = emptyIfNull(wordMeaningSource);
        this.exampleSentenceSource = emptyIfNull(exampleSentenceSource);
    }

    public String getWordMeaning() {
        return wordMeaning;
    }

    @XmlElement
    public void setWordMeaning(String wordMeaning) {
        this.wordMeaning = wordMeaning;
    }

    public String getExampleSentence() {
        return emptyIfNull(exampleSentence);
    }

    @XmlElement
    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getImgName() {
        return emptyIfNull(imgName);
    }

    @XmlElement
    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getExampleSentenceSource() {
        return emptyIfNull(exampleSentenceSource);
    }

    @XmlAttribute
    public void setExampleSentenceSource(String exampleSentenceSource) {
        this.exampleSentenceSource = exampleSentenceSource;
    }

    public String getWordMeaningSource() {
        return emptyIfNull(wordMeaningSource);
    }

    @XmlAttribute
    public void setWordMeaningSource(String wordMeaningSource) {
        this.wordMeaningSource = wordMeaningSource;
    }

    public String getComment() {
        return emptyIfNull(comment);
    }

    @XmlAttribute
    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addComment(String comment) {
        if (isNullOrEmpty(this.comment)) {
            this.comment = comment;
        }
        else {
            this.comment += "; " + comment;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Meaning meaning = (Meaning) o;

        return new EqualsBuilder()
                .append(wordMeaning, meaning.wordMeaning)
                .append(exampleSentence, meaning.exampleSentence)
                .append(imgName, meaning.imgName)
                .append(wordMeaningSource, meaning.wordMeaningSource)
                .append(exampleSentenceSource, meaning.exampleSentenceSource)
                .append(comment, meaning.comment)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(wordMeaning)
                .append(exampleSentence)
                .append(imgName)
                .append(wordMeaningSource)
                .append(exampleSentenceSource)
                .append(comment)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("wordMeaning", wordMeaning)
                .append("wordMeaningSource", wordMeaningSource)
                .append("exampleSentence", exampleSentence)
                .append("exampleSentenceSource", exampleSentenceSource)
                .append("imgName", imgName)
                .append("comment", comment)
                .toString();
    }

    private String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

}
