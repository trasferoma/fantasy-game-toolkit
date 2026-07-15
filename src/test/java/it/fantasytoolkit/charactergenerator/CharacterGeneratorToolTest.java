package it.fantasytoolkit.charactergenerator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.fantasytoolkit.charactergenerator.result.CharacterCharacteristic;
import it.fantasytoolkit.charactergenerator.result.CharacterResult;
import it.fantasytoolkitcore.core.model.Characteristic;
import it.fantasytoolkitcore.core.model.Race;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.FileReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharacterGeneratorToolTest {

    private static final int ITERATIONS = 50;

    static FileReader reader;

    @BeforeAll
    public static void setup() {
        reader = new FileReader();
    }

    @Test
    void generatesNameFromDictionaryOfRequestedRace() {
        Set<String> names = reader.readLines(Race.HUMAN.getNamesFile());

        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate();

        assertThat(result.race()).isEqualTo(Race.HUMAN);
        assertThat(result.name()).isNotBlank();
        assertThat(names).contains(result.name());
    }

    @Test
    void randomRaceAlwaysProducesAKnownRace() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .randomRace()
                    .characteristics(List.of(Characteristic.STRENGTH))
                    .totalPoints(1)
                    .generate();

            assertThat(result.race()).isIn((Object[]) Race.values());
        }
    }

    @Test
    void allCharacteristicsProducesExactlyTheSevenCharacteristics() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.ELF)
                .allCharacteristics()
                .totalPoints(Characteristic.values().length)
                .generate();

        Set<Characteristic> generatedCharacteristics = extractCharacteristics(result);

        assertThat(generatedCharacteristics).containsExactlyInAnyOrder(Characteristic.values());
        assertThat(result.characteristics()).hasSize(Characteristic.values().length);
    }

    @Test
    void characteristicsListIsDeduplicated() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.ORC)
                .characteristics(List.of(Characteristic.STRENGTH, Characteristic.STRENGTH, Characteristic.AGILITY))
                .totalPoints(2)
                .generate();

        Set<Characteristic> generatedCharacteristics = extractCharacteristics(result);

        assertThat(generatedCharacteristics).containsExactlyInAnyOrder(Characteristic.STRENGTH, Characteristic.AGILITY);
        assertThat(result.characteristics()).hasSize(2);
    }

    @Test
    void sumOfCharacteristicValuesAlwaysEqualsTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.UNDEAD)
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            int sum = sumOfValues(result);

            assertThat(sum).isEqualTo(20);
        }
    }

    @Test
    void eachValueRespectsDefaultMinimumOfOne() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.HUMAN)
                    .allCharacteristics()
                    .totalPoints(Characteristic.values().length)
                    .generate();

            assertThat(result.characteristics())
                    .extracting(CharacterCharacteristic::value)
                    .allMatch(value -> value >= 1);
        }
    }

    @Test
    void eachValueRespectsCustomMinimumOfZero() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.HUMAN)
                    .allCharacteristics()
                    .totalPoints(0)
                    .minCharacteristicValue(0)
                    .generate();

            assertThat(result.characteristics())
                    .extracting(CharacterCharacteristic::value)
                    .allMatch(value -> value >= 0);
        }
    }

    @Test
    void eachValueRespectsCustomMinimumOfThree() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.HUMAN)
                    .allCharacteristics()
                    .totalPoints(Characteristic.values().length * 3)
                    .minCharacteristicValue(3)
                    .generate();

            assertThat(result.characteristics())
                    .extracting(CharacterCharacteristic::value)
                    .allMatch(value -> value >= 3);
        }
    }

    @Test
    void threeCharacteristicsWithMinOneAndFourTotalPointsSumToFour() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.HUMAN)
                    .characteristics(List.of(Characteristic.STRENGTH, Characteristic.AGILITY, Characteristic.LUCK))
                    .totalPoints(4)
                    .generate();

            int sum = sumOfValues(result);

            assertThat(sum).isEqualTo(4);
            assertThat(result.characteristics())
                    .extracting(CharacterCharacteristic::value)
                    .allMatch(value -> value >= 1);
        }
    }

    @Test
    void nicknameProducesNameComposedOfKnownRaceNameAndKnownNickname() {
        Set<String> names = reader.readLines(Race.HUMAN.getNamesFile());
        Set<String> nicknames = reader.readLines("/namegenerator/nicknames.txt");

        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .addNickname()
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate();

        assertThat(result.name()).isNotBlank().contains(" ");

        boolean composedOfKnownNameAndNickname = names.stream()
                .anyMatch(name -> isComposedOf(result.name(), name, nicknames));

        assertThat(composedOfKnownNameAndNickname)
                .as("Generated name must be a known race name followed by a known nickname")
                .isTrue();
    }

    @Test
    void generateWithoutRaceThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Race must be set before generating a character");
    }

    @Test
    void generateWithBothRaceAndRandomRaceThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .randomRace()
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of race or randomRace can be used together");
    }

    @Test
    void generateWithoutCharacteristicsSourceThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("One of characteristics or allCharacteristics must be set before generating a character");
    }

    @Test
    void generateWithBothCharacteristicsAndAllCharacteristicsThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characteristics(List.of(Characteristic.STRENGTH))
                .allCharacteristics()
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of characteristics or allCharacteristics can be used together");
    }

    @Test
    void generateWithoutTotalPointsThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characteristics(List.of(Characteristic.STRENGTH))
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Total points must be set before generating a character");
    }

    @Test
    void generateWithNotEnoughTotalPointsThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characteristics(List.of(Characteristic.STRENGTH, Characteristic.AGILITY))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Total points (1) are not enough to give each of the 2 characteristics"
                        + " the minimum value of 1");
    }

    @Test
    void generateWithNegativeMinCharacteristicValueThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .minCharacteristicValue(-1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Minimum characteristic value must not be negative");
    }

    private Set<Characteristic> extractCharacteristics(CharacterResult result) {
        return result.characteristics().stream()
                .map(CharacterCharacteristic::characteristic)
                .collect(Collectors.toSet());
    }

    private int sumOfValues(CharacterResult result) {
        return result.characteristics().stream()
                .mapToInt(CharacterCharacteristic::value)
                .sum();
    }

    private boolean isComposedOf(String generatedName, String name, Set<String> nicknames) {
        String prefix = name + " ";
        if (!generatedName.startsWith(prefix)) {
            return false;
        }
        String nickname = generatedName.substring(prefix.length());
        return nicknames.contains(nickname);
    }
}
