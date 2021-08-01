package org.vorin.bestwords.loaders;

import org.junit.Test;
import org.vorin.bestwords.model.Dictionary;

import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

public class SynonymStoreTest {

    private final SynonymStore synonymStore = new SynonymStore(Dictionary.EN_PL, new SynonimNetParser());

    @Test
    public void groupBySynonyms() {
        // given
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");
        synonymStore.addMeaning("dryndac", "dzwonic", "");
        synonymStore.addMeaning("dzwonic", "dryndac", "");
        synonymStore.addMeaning("dryndac", "wspolny synonim", "");
        synonymStore.addMeaning("auto", "wspolny synonim", "");

        synonymStore.addMeaning("auto", "", "");
        synonymStore.addMeaning("idiota", "", "");

        // when & then
        assertThat(synonymStore.groupBySynonyms(List.of("auto", "idiota", "dryndac", "telefonowac")),
                is(List.of(List.of("auto"),
                           List.of("idiota"),
                           List.of("dryndac", "telefonowac"))));
        assertThat(synonymStore.groupBySynonyms(List.of("auto", "duren", "idiota", "dzwonic", "dryndac")),
                is(List.of(List.of("auto"),
                           List.of("duren", "idiota"),
                           List.of("dzwonic", "dryndac"))));
        assertThat(synonymStore.groupBySynonyms(List.of("auto", "idiota", "duren", "dzwonic", "wspolny synonim", "dryndac")),
                is(List.of(List.of("auto", "wspolny synonim"),
                           List.of("idiota", "duren"),
                           List.of("dzwonic", "dryndac"),
                           List.of("wspolny synonim", "auto", "dryndac"),
                           List.of("dryndac", "dzwonic", "wspolny synonim"))));
    }

    @Test
    public void findSynonymsInList() {
        // given
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");
        synonymStore.addMeaning("dryndac", "dzwonic", "");
        synonymStore.addMeaning("dzwonic", "dryndac", "");
        synonymStore.addMeaning("dryndac", "wspolny synonim", "");
        synonymStore.addMeaning("auto", "wspolny synonim", "");

        synonymStore.addMeaning("auto", "", "");
        synonymStore.addMeaning("idiota", "", "");

        // when & then
        assertThat(synonymStore.findSynonymsInList("auto", List.of()),
                                                is(List.of("auto")));
        assertThat(synonymStore.findSynonymsInList("auto", List.of("auto", "idiota", "dryndac", "telefonowac")),
                                                is(List.of("auto")));
        assertThat(synonymStore.findSynonymsInList("duren", List.of("auto", "idiota", "dryndac", "telefonowac")),
                                                is(List.of("duren", "idiota")));
        assertThat(synonymStore.findSynonymsInList("telefonowac", List.of("auto", "idiota", "dryndac", "telefonowac")),
                                                is(List.of("telefonowac", "dryndac")));
        assertThat(synonymStore.findSynonymsInList("dryndac", List.of("auto", "duren", "idiota", "dzwonic", "wspolny synonim", "dryndac")),
                                                is(List.of("dryndac", "dzwonic", "wspolny synonim")));
    }

    @Test
    public void removeSynonyms() {
        // given
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");
        synonymStore.addMeaning("dryndac", "dzwonic", "");

        synonymStore.addMeaning("auto", "", "");
        synonymStore.addMeaning("idiota", "", "");
        synonymStore.addMeaning("dzwonic", "dryndac", "");

        // when & then
        assertThat(synonymStore.removeSynonymsFromList(List.of("auto", "idiota", "dryndac", "telefonowac")),
                                                    is(List.of("auto", "idiota", "dryndac")));
        assertThat(synonymStore.removeSynonymsFromList(List.of("duren", "idiota", "auto", "dryndac")),
                                                    is(List.of("duren", "auto", "dryndac")));
        assertThat(synonymStore.removeSynonymsFromList(List.of("auto", "idiota", "telefonowac", "duren", "dryndac")),
                                                    is(List.of("auto", "duren", "dryndac")));
        assertThat(synonymStore.removeSynonymsFromList(List.of("auto", "idiota", "dzwonic", "duren", "dryndac")),
                                                    is(List.of("auto", "dzwonic", "duren")));
    }

    @Test
    public void removeSynonyms2() {
        // given
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");
        synonymStore.addMeaning("dryndac", "dzwonic", "");

        synonymStore.addMeaning("auto", "samochod", "");
        synonymStore.addMeaning("idiota", "duren", "");
        synonymStore.addMeaning("telefonowac", "dryndac", "");

        // when & then
        assertThat(synonymStore.removeSynonymsFromList(List.of("auto", "idiota", "dryndac", "telefonowac")),
                                                    is(List.of("auto", "idiota", "dryndac")));
        assertThat(synonymStore.removeSynonymsFromList(List.of("duren", "idiota", "auto", "dryndac")),
                                                    is(List.of("duren", "auto", "dryndac")));
        assertThat(synonymStore.removeSynonymsFromList(List.of("auto", "idiota", "telefonowac", "duren", "dryndac")),
                                                    is(List.of("auto", "idiota", "telefonowac")));
    }

}