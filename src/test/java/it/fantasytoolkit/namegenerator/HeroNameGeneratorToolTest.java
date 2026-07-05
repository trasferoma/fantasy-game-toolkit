package it.fantasytoolkit.namegenerator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class HeroNameGeneratorToolTest extends AbstractNameGeneratorTest {

    @Test
    void generatesNameComposedOfKnownNameAndAdjective() {
        Set<String> names = readLines("/namegenerator/heros_names.txt");
        Set<String> adjectives = readLines("/namegenerator/hero_suffix.txt");

        String generated = HeroNameGeneratorTool.generateName();

        assertThat(generated)
                .as("Generated name must not be blank")
                .isNotBlank()
                .contains(" ");

        String[] parts = generated.split("\\s+", 2);
        assertThat(parts).hasSize(2);
        assertThat(names).as("Name not present in dictionary").contains(parts[0]);
        assertThat(adjectives).as("Adjective not present in dictionary").contains(parts[1]);
    }

    @Test
    void batchGenerationAlwaysReturnsNonBlankNames() {
        for (int i = 0; i < 10; i++) {
            assertThat(HeroNameGeneratorTool.generateName())
                    .as("Generated name must not be blank")
                    .isNotBlank();
        }
    }
}
