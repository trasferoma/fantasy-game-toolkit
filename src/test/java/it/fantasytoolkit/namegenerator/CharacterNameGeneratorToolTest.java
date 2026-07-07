package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.model.Race;
import it.fantasytoolkit.namegenerator.result.NameResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.FileReader;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharacterNameGeneratorToolTest {
    static FileReader reader;

    @BeforeAll
    public static void setup() {
        reader = new FileReader();
    }

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
        Set<String> names = reader.readLines(Race.HUMAN.getNamesFile());
        Set<String> nicknames = reader.readLines("/namegenerator/nicknames.txt");

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
    void generateWithoutRaceThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterNameGeneratorTool.addNickname().generate())
                .isInstanceOf(IllegalStateException.class);
    }

    private void assertGeneratedNameIsKnown(Race race) {
        Set<String> names = reader.readLines(race.getNamesFile());

        NameResult result = CharacterNameGeneratorTool
                .race(race)
                .generate();

        assertThat(result.name())
                .as("Generated name must not be blank")
                .isNotBlank();
        assertThat(names).as("Name not present in dictionary").contains(result.name());
    }
}
