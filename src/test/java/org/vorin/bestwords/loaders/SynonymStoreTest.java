package org.vorin.bestwords.loaders;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

public class SynonymStoreTest {

    @Test
    public void removeSynonyms() {
        // given
        var synonymStore = new SynonymStore();
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");
        synonymStore.addMeaning("dryndac", "dzwonic", "");

        synonymStore.addMeaning("auto", "", "");
        synonymStore.addMeaning("idiota", "", "");
        synonymStore.addMeaning("dzwonic", "dryndac", "");

        // when & then
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "dryndac", "telefonowac")),
                is(Arrays.asList("auto", "idiota", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("duren", "idiota", "auto", "dryndac")),
                   is(Arrays.asList("duren", "auto", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "telefonowac", "duren", "dryndac")),
                is(Arrays.asList("auto", "duren", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "dzwonic", "duren", "dryndac")),
                is(Arrays.asList("auto", "dzwonic", "duren")));
    }

    @Test
    public void removeSynonyms2() {
        // given
        var synonymStore = new SynonymStore();
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");
        synonymStore.addMeaning("dryndac", "dzwonic", "");

        synonymStore.addMeaning("auto", "samochod", "");
        synonymStore.addMeaning("idiota", "duren", "");
        synonymStore.addMeaning("telefonowac", "dryndac", "");

        // when & then
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "dryndac", "telefonowac")),
                is(Arrays.asList("auto", "idiota", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("duren", "idiota", "auto", "dryndac")),
                is(Arrays.asList("duren", "auto", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "telefonowac", "duren", "dryndac")),
                is(Arrays.asList("auto", "idiota", "telefonowac")));
    }

}