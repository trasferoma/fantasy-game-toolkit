package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.model.Race;
import it.fantasytoolkit.core.types.Seed;
import it.fantasytoolkit.namegenerator.result.NameResult;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharacterNameGeneratorToolTest extends AbstractNameGeneratorTest {

    @Test
    void generatesHumanNameFromDictionaryWithoutNickname() {
        assertGeneratedNameIsKnown(Race.HUMAN);
    }

    @Test
    void generatesElfNameFromDictionaryWithoutNickname() {
        assertGeneratedNameIsKnown(Race.ELF);
    }

    @Test
    void generatesOrcNameFromDictionaryWithoutNickname() {
        assertGeneratedNameIsKnown(Race.ORC);
    }

    @Test
    void generatesUndeadNameFromDictionaryWithoutNickname() {
        assertGeneratedNameIsKnown(Race.UNDEAD);
    }

    @Test
    void generatesNameComposedOfRaceNameAndNicknameFromDictionaries() {
        Set<String> names = readLines(Race.HUMAN.getNamesFile());
        Set<String> nicknames = readLines("/namegenerator/nicknames.txt");

        NameResult result = CharacterNameGeneratorTool.addNickname().race(Race.HUMAN).generate();

        assertThat(result.name())
                .as("Generated name must not be blank")
                .isNotBlank()
                .contains(" ");

        String[] parts = result.name().split("\\s+", 2);
        assertThat(parts).hasSize(2);
        assertThat(names).as("Name not present in dictionary").contains(parts[0]);
        assertThat(nicknames).as("Nickname not present in dictionary").contains(parts[1]);
    }

    @Test
    void sameSeedProducesSameGeneratedNameWithoutNickname() {
        NameResult first = CharacterNameGeneratorTool.race(Race.ELF).generate();
        Seed seed = first.seed();

        NameResult again = CharacterNameGeneratorTool.race(Race.ELF).useSeed(seed).generate();

        assertThat(again.name()).isEqualTo(first.name());
    }

    @Test
    void sameSeedProducesSameGeneratedNameWithNickname() {
        NameResult first = CharacterNameGeneratorTool.addNickname()
                .race(Race.ORC)
                .generate();

        Seed seed = first.seed();

        NameResult again = CharacterNameGeneratorTool
                .addNickname()
                .race(Race.ORC)
                .useSeed(seed)
                .generate();

        assertThat(again.name()).isEqualTo(first.name());
    }

    @Test
    void generateWithoutRaceThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterNameGeneratorTool.addNickname().generate())
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertGeneratedNameIsKnown(Race race) {
        Set<String> names = readLines(race.getNamesFile());

        NameResult result = CharacterNameGeneratorTool.race(race).generate();

        assertThat(result.name())
                .as("Generated name must not be blank")
                .isNotBlank();
        assertThat(result.seed()).as("Generated result must expose a seed").isNotNull();
        assertThat(names).as("Name not present in dictionary").contains(result.name());
    }
}
