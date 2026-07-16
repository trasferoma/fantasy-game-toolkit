package it.fantasytoolkit.charactergenerator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.fantasytoolkit.charactergenerator.result.CharacterCharacteristic;
import it.fantasytoolkit.charactergenerator.result.CharacterResult;
import it.fantasytoolkitcore.core.model.CharacterClass;
import it.fantasytoolkitcore.core.model.Characteristic;
import it.fantasytoolkitcore.core.model.ClassBonusTable;
import it.fantasytoolkitcore.core.model.Race;
import it.fantasytoolkitcore.core.model.RaceBonusTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.FileReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharacterGeneratorToolTest {

    private static final int ITERATIONS = 50;

    static FileReader reader;

    private PrintStream originalSystemOut;
    private ByteArrayOutputStream capturedOutput;

    @BeforeAll
    public static void setup() {
        reader = new FileReader();
    }
/*
    @BeforeEach
    void captureSystemOut() {
        originalSystemOut = System.out;
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
    }
*/
    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalSystemOut);
    }

    @Test
    void simple() {
        CharacterResult result = CharacterGeneratorTool.building()
                .randomRace()
                .randomClass()
                .allCharacteristics()
                .totalPoints(10)
                .verbose()
                .generate();

        System.out.println(result);

    }

    @Test
    void generatesNameFromDictionaryOfRequestedRace() {
        Set<String> names = reader.readLines(Race.HUMAN.getNamesFile());

        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .raceBonusTable(RaceBonusTable.builder().build())
                .classBonusTable(ClassBonusTable.builder().build())
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
                    .characterClass(CharacterClass.WARRIOR)
                    .characteristics(List.of(Characteristic.STRENGTH))
                    .totalPoints(1)
                    .raceBonusTable(RaceBonusTable.builder().build())
                    .classBonusTable(ClassBonusTable.builder().build())
                    .generate();

            assertThat(result.race()).isIn((Object[]) Race.values());
        }
    }

    @Test
    void allCharacteristicsProducesExactlyTheSevenCharacteristics() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.ELF)
                .characterClass(CharacterClass.WARRIOR)
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
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH, Characteristic.STRENGTH, Characteristic.AGILITY))
                .totalPoints(2)
                .raceBonusTable(RaceBonusTable.builder().build())
                .classBonusTable(ClassBonusTable.builder().build())
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
                    .characterClass(CharacterClass.WARRIOR)
                    .allCharacteristics()
                    .totalPoints(20)
                    .raceBonusTable(RaceBonusTable.builder().build())
                    .classBonusTable(ClassBonusTable.builder().build())
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
                    .characterClass(CharacterClass.WARRIOR)
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
                    .characterClass(CharacterClass.WARRIOR)
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
                    .characterClass(CharacterClass.WARRIOR)
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
                    .characterClass(CharacterClass.WARRIOR)
                    .characteristics(List.of(Characteristic.STRENGTH, Characteristic.AGILITY, Characteristic.LUCK))
                    .totalPoints(4)
                    .raceBonusTable(RaceBonusTable.builder().build())
                    .classBonusTable(ClassBonusTable.builder().build())
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
                .characterClass(CharacterClass.WARRIOR)
                .addNickname()
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .raceBonusTable(RaceBonusTable.builder().build())
                .classBonusTable(ClassBonusTable.builder().build())
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
    void generateWithoutClassThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Character class must be set before generating a character");
    }

    @Test
    void generateWithBothClassAndRandomClassThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
                .randomClass()
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only one of characterClass or randomClass can be used together");
    }

    @Test
    void generateWithoutCharacteristicsSourceThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("One of characteristics or allCharacteristics must be set before generating a character");
    }

    @Test
    void generateWithBothCharacteristicsAndAllCharacteristicsThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
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
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH))
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Total points must be set before generating a character");
    }

    @Test
    void generateWithNotEnoughTotalPointsThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
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
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .minCharacteristicValue(-1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Minimum characteristic value must not be negative");
    }

    @Test
    void defaultOrcBonusAddsThreePointsOnTopOfTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.ORC)
                    .characterClass(CharacterClass.WARRIOR)
                    .classBonusTable(ClassBonusTable.builder().build())
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            assertThat(sumOfValues(result)).isEqualTo(23);
            assertThat(result.characteristics()).hasSize(Characteristic.values().length);
        }
    }

    @Test
    void defaultHumanBonusAddsThreePointsOnTopOfTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.HUMAN)
                    .characterClass(CharacterClass.WARRIOR)
                    .classBonusTable(ClassBonusTable.builder().build())
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            assertThat(sumOfValues(result)).isEqualTo(23);
        }
    }

    @Test
    void defaultElfBonusAddsThreePointsOnTopOfTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.ELF)
                    .characterClass(CharacterClass.WARRIOR)
                    .classBonusTable(ClassBonusTable.builder().build())
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            assertThat(sumOfValues(result)).isEqualTo(23);
        }
    }

    @Test
    void defaultUndeadBonusAddsThreePointsOnTopOfTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.UNDEAD)
                    .characterClass(CharacterClass.WARRIOR)
                    .classBonusTable(ClassBonusTable.builder().build())
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            assertThat(sumOfValues(result)).isEqualTo(23);
        }
    }

    @Test
    void defaultOrcBonusAppliesExactValuesToTargetedCharacteristics() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.ORC)
                .characterClass(CharacterClass.WARRIOR)
                .classBonusTable(ClassBonusTable.builder().build())
                .characteristics(List.of(Characteristic.STRENGTH, Characteristic.RESISTANCE))
                .minCharacteristicValue(0)
                .totalPoints(0)
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(2);
        assertThat(valueOf(result, Characteristic.RESISTANCE)).isEqualTo(1);
    }

    @Test
    void customRaceBonusTableOverridesTheDefaultOne() {
        RaceBonusTable customTable = RaceBonusTable.builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 5)
                .build();

        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
                .classBonusTable(ClassBonusTable.builder().build())
                .characteristics(List.of(Characteristic.STRENGTH))
                .minCharacteristicValue(0)
                .totalPoints(0)
                .raceBonusTable(customTable)
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(5);
    }

    @Test
    void emptyRaceBonusTableOptsOutOfAnyBonus() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .characterClass(CharacterClass.WARRIOR)
                .classBonusTable(ClassBonusTable.builder().build())
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .raceBonusTable(RaceBonusTable.builder().build())
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(1);
        assertThat(sumOfValues(result)).isEqualTo(1);
    }

    @Test
    void bonusTargetingAbsentCharacteristicThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.ORC)
                .characterClass(CharacterClass.WARRIOR)
                .classBonusTable(ClassBonusTable.builder().build())
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESISTANCE");
    }

    @Test
    void customRaceBonusTableWithoutEntryForTheRaceProducesNoBonus() {
        RaceBonusTable customTable = RaceBonusTable.builder()
                .bonus(Race.HUMAN, Characteristic.STRENGTH, 5)
                .build();

        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.ELF)
                .characterClass(CharacterClass.WARRIOR)
                .classBonusTable(ClassBonusTable.builder().build())
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .raceBonusTable(customTable)
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(1);
    }

    @Test
    void defaultWarriorBonusAppliesExactValuesToTargetedCharacteristics() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .raceBonusTable(RaceBonusTable.builder().build())
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH, Characteristic.STAMINA))
                .minCharacteristicValue(0)
                .totalPoints(0)
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(2);
        assertThat(valueOf(result, Characteristic.STAMINA)).isEqualTo(1);
    }

    @Test
    void defaultClassBonusAddsThreePointsOnTopOfTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.HUMAN)
                    .raceBonusTable(RaceBonusTable.builder().build())
                    .characterClass(CharacterClass.MAGE)
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            assertThat(sumOfValues(result)).isEqualTo(23);
        }
    }

    @Test
    void customClassBonusTableOverridesTheDefaultOne() {
        ClassBonusTable customTable = ClassBonusTable.builder()
                .bonus(CharacterClass.WARRIOR, Characteristic.STRENGTH, 5)
                .build();

        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .raceBonusTable(RaceBonusTable.builder().build())
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH))
                .minCharacteristicValue(0)
                .totalPoints(0)
                .classBonusTable(customTable)
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(5);
    }

    @Test
    void emptyClassBonusTableOptsOutOfAnyBonus() {
        CharacterResult result = CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .raceBonusTable(RaceBonusTable.builder().build())
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .classBonusTable(ClassBonusTable.builder().build())
                .generate();

        assertThat(valueOf(result, Characteristic.STRENGTH)).isEqualTo(1);
        assertThat(sumOfValues(result)).isEqualTo(1);
    }

    @Test
    void classBonusTargetingAbsentCharacteristicThrowsIllegalStateException() {
        assertThatThrownBy(() -> CharacterGeneratorTool.building()
                .race(Race.HUMAN)
                .raceBonusTable(RaceBonusTable.builder().build())
                .characterClass(CharacterClass.WARRIOR)
                .characteristics(List.of(Characteristic.STRENGTH))
                .totalPoints(1)
                .generate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STAMINA");
    }

    @Test
    void bothDefaultRaceAndClassBonusesCombineOnTopOfTotalPoints() {
        for (int i = 0; i < ITERATIONS; i++) {
            CharacterResult result = CharacterGeneratorTool.building()
                    .race(Race.ORC)
                    .characterClass(CharacterClass.WARRIOR)
                    .allCharacteristics()
                    .totalPoints(20)
                    .generate();

            assertThat(sumOfValues(result)).isEqualTo(26);
        }
    }

    @Test
    void verboseLogsRaceAndClassBonusDetails() {
        CharacterGeneratorTool.building()
                .race(Race.ORC)
                .characterClass(CharacterClass.WARRIOR)
                .allCharacteristics()
                .totalPoints(20)
                .verbose()
                .generate();

        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        assertThat(output).contains("race ORC", "total +3", "STRENGTH +2", "RESISTANCE +1");
        assertThat(output).contains("character class WARRIOR", "STAMINA +1");
        assertThat(output).contains("Resolved race", "Resolved class", "After distribution", "Final character");
    }

    @Test
    void nonVerboseProducesNoOutput() {
        CharacterGeneratorTool.building()
                .race(Race.ORC)
                .characterClass(CharacterClass.WARRIOR)
                .allCharacteristics()
                .totalPoints(20)
                .generate();

        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        assertThat(output).isEmpty();
    }

    @Test
    void verboseLogsNoBonusWhenTableEmpty() {
        CharacterGeneratorTool.building()
                .race(Race.ORC)
                .characterClass(CharacterClass.WARRIOR)
                .raceBonusTable(RaceBonusTable.builder().build())
                .classBonusTable(ClassBonusTable.builder().build())
                .allCharacteristics()
                .totalPoints(20)
                .verbose()
                .generate();

        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        assertThat(output).contains("No bonus from race", "No bonus from character class");
    }

    private int valueOf(CharacterResult result, Characteristic characteristic) {
        return result.characteristics().stream()
                .filter(candidate -> candidate.characteristic() == characteristic)
                .mapToInt(CharacterCharacteristic::value)
                .findFirst()
                .orElseThrow();
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
