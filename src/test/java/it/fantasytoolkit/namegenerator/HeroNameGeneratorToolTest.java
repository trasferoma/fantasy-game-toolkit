package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.types.Seed;
import it.fantasytoolkit.namegenerator.result.NameResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class HeroNameGeneratorToolTest extends AbstractNameGeneratorTest {

    @Test
    void generatesNameComposedOfKnownNameAndAdjective() {
        Set<String> names = readLines("/namegenerator/heros_names.txt");
        Set<String> adjectives = readLines("/namegenerator/hero_suffix.txt");

        NameResult result = HeroNameGeneratorTool.generateName();

        assertThat(result.name())
                .as("Generated name must not be blank")
                .isNotBlank()
                .contains(" ");
        assertThat(result.seed()).as("Generated result must expose a seed").isNotNull();

        String[] parts = result.name().split("\\s+", 2);
        assertThat(parts).hasSize(2);
        assertThat(names).as("Name not present in dictionary").contains(parts[0]);
        assertThat(adjectives).as("Adjective not present in dictionary").contains(parts[1]);
    }

    @Test
    void batchGenerationAlwaysReturnsNonBlankNames() {
        for (int i = 0; i < 10; i++) {
            assertThat(HeroNameGeneratorTool.generateName().name())
                    .as("Generated name must not be blank")
                    .isNotBlank();
        }
    }

    @Test
    void sameSeedProducesSameGeneratedName() {
        NameResult first = HeroNameGeneratorTool.generateName();
        Seed seed = first.seed();

        NameResult again = HeroNameGeneratorTool.generateName(seed);

        assertThat(again.name()).isEqualTo(first.name());
    }
}
