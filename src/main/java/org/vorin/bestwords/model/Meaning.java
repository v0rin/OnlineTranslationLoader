package org.vorin.bestwords.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import static com.google.common.base.Preconditions.checkNotNull;

@XmlType(propOrder={"wordMeaning","exampleSentence","imgName"})
public class Meaning {

    private String wordMeaning;
    private String exampleSentence = "";
    private String imgName = "";

    public Meaning() {} // needed for JAXB unmarshalling

    public Meaning(String wordMeaning, String exampleSentence, String imgName) {
        checkNotNull(wordMeaning);
        this.wordMeaning = wordMeaning;
        this.exampleSentence = exampleSentence;
        this.imgName = imgName;
    }

    public String getWordMeaning() {
        return wordMeaning;
    }

    @XmlElement
    public void setWordMeaning(String wordMeaning) {
        this.wordMeaning = wordMeaning;
    }

    public String getExampleSentence() {
        return getEmptyIfNull(exampleSentence);
    }

    @XmlElement
    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getImgName() {
        return getEmptyIfNull(imgName);
    }

    @XmlElement
    public void setImgName(String imgName) {
        this.imgName = imgName;
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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(wordMeaning)
                .append(exampleSentence)
                .append(imgName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("wordMeaning", wordMeaning)
                .append("exampleSentence", exampleSentence)
                .append("imgName", imgName)
                .toString();
    }

    private String getEmptyIfNull(String s) {
        return s == null ? "" : s;
    }

}
