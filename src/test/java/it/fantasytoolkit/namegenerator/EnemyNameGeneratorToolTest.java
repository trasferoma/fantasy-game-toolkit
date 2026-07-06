package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.model.Race;
import it.fantasytoolkit.namegenerator.result.NameResult;
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

    @Test
    void sameSeedProducesSameGeneratedName() {
        NameResult first = EnemyNameGeneratorTool.generateName(Race.ELF);

        NameResult again = EnemyNameGeneratorTool.generateName(Race.ELF, first.seed());

        assertThat(again.name()).isEqualTo(first.name());
    }

    private void assertGeneratedNameIsKnown(Race race, String dictionaryResource) {
        Set<String> names = readLines(dictionaryResource);

        NameResult result = EnemyNameGeneratorTool.generateName(race);

        assertThat(result.name())
                .as("Generated name must not be blank")
                .isNotBlank();
        assertThat(result.seed()).as("Generated result must expose a seed").isNotNull();
        assertThat(names).as("Name not present in dictionary").contains(result.name());
    }
}
