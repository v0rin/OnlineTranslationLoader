package org.vorin.bestwords.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

@XmlType(propOrder={"foreignWord","pronunciation","audioName","meanings"})
public class Translation {

    protected String foreignWord;
    private String pronunciation;
    private String audioName;
    private List<Meaning> meanings;
    private String comment;

    public Translation() {} // needed for JAXB unmarshalling

    public Translation(String foreignWord,
                       String pronunciation,
                       String audioName,
                       List<Meaning> meanings) {
        checkNotNull(foreignWord);
        checkNotNull(pronunciation);
        checkNotNull(audioName);
        checkNotNull(meanings);
        this.foreignWord = foreignWord;
        this.pronunciation = pronunciation;
        this.audioName = audioName;
        this.meanings = meanings;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    @XmlElement
    public void setForeignWord(String foreignWord) {
        this.foreignWord = foreignWord;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    @XmlElement
    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getAudioName() {
        return audioName;
    }

    @XmlElement
    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public List<Meaning> getMeanings() {
        return meanings;
    }

    @XmlElementWrapper(name = "meanings")
    @XmlElement(name = "meaning")
    public void setMeanings(List<Meaning> meanings) {
        this.meanings = meanings;
    }

    public String getComment() {
        return comment;
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

        Translation that = (Translation) o;

        return new EqualsBuilder()
                .append(foreignWord, that.foreignWord)
                .append(pronunciation, that.pronunciation)
                .append(audioName, that.audioName)
                .append(meanings, that.meanings)
                .append(comment, that.comment)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(foreignWord)
                .append(pronunciation)
                .append(audioName)
                .append(meanings)
                .append(comment)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("foreignWord", foreignWord)
                .append("pronunciation", pronunciation)
                .append("audioName", audioName)
                .append("meaningList", meanings)
                .append("comment", comment)
                .toString();
    }

}

