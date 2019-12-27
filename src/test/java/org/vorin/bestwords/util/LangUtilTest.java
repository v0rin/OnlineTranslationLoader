package org.vorin.bestwords.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LangUtilTest {

    @Test
    public void getParsedForeignWord() {
        assertThat(LangUtil.getParsedForeignWord("a / an"), is("a"));
        assertThat(LangUtil.getParsedForeignWord("okay / OK"), is("okay"));
        assertThat(LangUtil.getParsedForeignWord("almost = nearly"), is("almost"));
        assertThat(LangUtil.getParsedForeignWord("bitch"), is("bitch"));
    }

    @Test
    public void santizeSpanishMeaning() {
        assertThat(LangUtil.santizeSpanishMeaning("tener la intención"), is("tener la intención"));
        assertThat(LangUtil.santizeSpanishMeaning("llevar algo a"), is("llevar"));
        assertThat(LangUtil.santizeSpanishMeaning("querer decir"), is("querer decir"));
        assertThat(LangUtil.santizeSpanishMeaning("hacer algo a propósito"), is("hacer"));
        assertThat(LangUtil.santizeSpanishMeaning("significar que"), is("significar"));
        assertThat(LangUtil.santizeSpanishMeaning("querer decir que"), is(""));
        assertThat(LangUtil.santizeSpanishMeaning("referirse a"), is("referirse"));
        assertThat(LangUtil.santizeSpanishMeaning("mezquino/a"), is("mezquino/a"));
        assertThat(LangUtil.santizeSpanishMeaning("hacer venir a"), is(""));
        assertThat(LangUtil.santizeSpanishMeaning("hacerle a alguien hacer algo"), is("hacerle"));
        assertThat(LangUtil.santizeSpanishMeaning("amar hacer algo"), is(""));
        assertThat(LangUtil.santizeSpanishMeaning("-"), is(""));
        assertThat(LangUtil.santizeSpanishMeaning("tener la ocasión de"), is(""));
        assertThat(LangUtil.santizeSpanishMeaning("de la misma manera"), is(""));
        assertThat(LangUtil.santizeSpanishMeaning("a la derecha"), is("a la derecha"));
        assertThat(LangUtil.santizeSpanishMeaning("lo bueno"), is("lo bueno"));
        assertThat(LangUtil.santizeSpanishMeaning("la derecha"), is("la derecha"));
        assertThat(LangUtil.santizeSpanishMeaning("encargarse de"), is("encargarse"));
        assertThat(LangUtil.santizeSpanishMeaning("ayudar a alguien con"), is("ayudar"));
        assertThat(LangUtil.santizeSpanishMeaning("ayudar con"), is("ayudar"));
        assertThat(LangUtil.santizeSpanishMeaning("ayudar en"), is("ayudar"));
        assertThat(LangUtil.santizeSpanishMeaning("loco de"), is("loco"));
        assertThat(LangUtil.santizeSpanishMeaning("tomar el pelo"), is("tomar el pelo"));
        assertThat(LangUtil.santizeSpanishMeaning("un montón"), is("un montón"));
        assertThat(LangUtil.santizeSpanishMeaning("el cual"), is("el cual"));
        assertThat(LangUtil.santizeSpanishMeaning("hijo de puta"), is("hijo de puta"));
    }

    @Test
    public void wordCount() {
        assertThat(LangUtil.wordCount("tener la ba"), is(3));
        assertThat(LangUtil.wordCount(""), is(0));
        assertThat(LangUtil.wordCount("tener la"), is(2));
        assertThat(LangUtil.wordCount("tener"), is(1));
        assertThat(LangUtil.wordCount("tener la intención"), is(3));
    }

}