package org.vorin.bestwords.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LangUtilTest {

    @Test
    public void sanitizeWord() {
        assertThat(LangUtil.sanitizeWord("a / an"), is("a"));
        assertThat(LangUtil.sanitizeWord("okay / OK"), is("okay"));
        assertThat(LangUtil.sanitizeWord("almost = nearly"), is("almost"));
        assertThat(LangUtil.sanitizeWord("almost (nearly)"), is("almost"));
        assertThat(LangUtil.sanitizeWord("bitch"), is("bitch"));
    }

    @Test
    public void santizeSpanishMeaning() {
        assertThat(LangUtil.sanitizeSpanishMeaning("tener la intención"), is("tener la intención"));
        assertThat(LangUtil.sanitizeSpanishMeaning("llevar algo a"), is("llevar"));
        assertThat(LangUtil.sanitizeSpanishMeaning("querer decir"), is("querer decir"));
        assertThat(LangUtil.sanitizeSpanishMeaning("hacer algo a propósito"), is("hacer"));
        assertThat(LangUtil.sanitizeSpanishMeaning("significar que"), is("significar"));
        assertThat(LangUtil.sanitizeSpanishMeaning("querer decir que"), is(""));
        assertThat(LangUtil.sanitizeSpanishMeaning("referirse a"), is("referirse"));
        assertThat(LangUtil.sanitizeSpanishMeaning("mezquino/a"), is("mezquino/a"));
        assertThat(LangUtil.sanitizeSpanishMeaning("hacer venir a"), is(""));
        assertThat(LangUtil.sanitizeSpanishMeaning("hacerle a alguien hacer algo"), is("hacerle"));
        assertThat(LangUtil.sanitizeSpanishMeaning("amar hacer algo"), is(""));
        assertThat(LangUtil.sanitizeSpanishMeaning("-"), is(""));
        assertThat(LangUtil.sanitizeSpanishMeaning("tener la ocasión de"), is(""));
        assertThat(LangUtil.sanitizeSpanishMeaning("de la misma manera"), is(""));
        assertThat(LangUtil.sanitizeSpanishMeaning("a la derecha"), is("a la derecha"));
        assertThat(LangUtil.sanitizeSpanishMeaning("lo bueno"), is("lo bueno"));
        assertThat(LangUtil.sanitizeSpanishMeaning("la derecha"), is("la derecha"));
        assertThat(LangUtil.sanitizeSpanishMeaning("encargarse de"), is("encargarse"));
        assertThat(LangUtil.sanitizeSpanishMeaning("ayudar a alguien con"), is("ayudar"));
        assertThat(LangUtil.sanitizeSpanishMeaning("ayudar con"), is("ayudar"));
        assertThat(LangUtil.sanitizeSpanishMeaning("ayudar en"), is("ayudar"));
        assertThat(LangUtil.sanitizeSpanishMeaning("loco de"), is("loco"));
        assertThat(LangUtil.sanitizeSpanishMeaning("tomar el pelo"), is("tomar el pelo"));
        assertThat(LangUtil.sanitizeSpanishMeaning("un montón"), is("un montón"));
        assertThat(LangUtil.sanitizeSpanishMeaning("el cual"), is("el cual"));
        assertThat(LangUtil.sanitizeSpanishMeaning("hijo de puta"), is("hijo de puta"));
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