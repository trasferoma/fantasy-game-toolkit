package it.fantasytoolkit.namegenerator.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameResultTest {

    @Test
    void builderCreatesResultWithNameAndSeed() {
        NameResult result = NameResult.builder()
                .name("Aragorn")
                .build();

        assertThat(result.name()).isEqualTo("Aragorn");
    }
}
