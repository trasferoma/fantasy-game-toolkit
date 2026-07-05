package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.model.Race;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EnemyNameGeneratorToolTest extends AbstractNameGeneratorTest {

    @Test
    void generatesElfNameFromDictionary() {
        assertGeneratedNameIsKnown(Race.ELF, "/namegenerator/elves_names.txt");
    }

    @Test
    void generatesOrcNameFromDictionary() {
        assertGeneratedNameIsKnown(Race.ORC, "/namegenerator/orks_names.txt");
    }

    @Test
    void generatesHumanNameFromDictionary() {
        assertGeneratedNameIsKnown(Race.HUMAN, "/namegenerator/humans_names.txt");
    }

    @Test
    void generatesUndeadNameFromDictionary() {
        assertGeneratedNameIsKnown(Race.UNDEAD, "/namegenerator/undeads_names.txt");
    }

    private void assertGeneratedNameIsKnown(Race race, String dictionaryResource) {
        Set<String> names = readLines(dictionaryResource);

        String generated = EnemyNameGeneratorTool.generateName(race);

        assertThat(generated)
                .as("Generated name must not be blank")
                .isNotBlank();
        assertThat(names).as("Name not present in dictionary").contains(generated);
    }
}
