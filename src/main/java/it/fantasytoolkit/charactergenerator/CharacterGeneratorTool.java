package it.fantasytoolkit.charactergenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import it.fantasytoolkitcore.core.model.CharacterClass;
import it.fantasytoolkitcore.core.model.Characteristic;
import it.fantasytoolkitcore.core.model.ClassBonusTable;
import it.fantasytoolkitcore.core.model.Race;
import it.fantasytoolkitcore.core.model.RaceBonusTable;
import it.fantasytoolkit.charactergenerator.result.CharacterCharacteristic;
import it.fantasytoolkit.charactergenerator.result.CharacterResult;
import it.fantasytoolkit.namegenerator.CharacterNameGeneratorTool;

public final class CharacterGeneratorTool {

    private CharacterGeneratorTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private Race race;
        private boolean randomRace;
        private CharacterClass characterClass;
        private boolean randomClass;
        private boolean withNickname;
        private boolean verbose;
        private List<Characteristic> characteristics;
        private boolean allCharacteristics;
        private int totalPoints;
        private boolean totalPointsSet;
        private int minCharacteristicValue = 1;
        private RaceBonusTable raceBonusTable = RaceBonusTable.withDefaultBonuses();
        private ClassBonusTable classBonusTable = ClassBonusTable.withDefaultBonuses();

        private Builder() {
        }

        public Builder race(Race race) {
            this.race = race;
            return this;
        }

        public Builder randomRace() {
            this.randomRace = true;
            return this;
        }

        public Builder characterClass(CharacterClass characterClass) {
            this.characterClass = characterClass;
            return this;
        }

        public Builder randomClass() {
            this.randomClass = true;
            return this;
        }

        public Builder addNickname() {
            this.withNickname = true;
            return this;
        }

        public Builder verbose() {
            this.verbose = true;
            return this;
        }

        public Builder characteristics(List<Characteristic> characteristics) {
            this.characteristics = characteristics;
            return this;
        }

        public Builder allCharacteristics() {
            this.allCharacteristics = true;
            return this;
        }

        public Builder totalPoints(int totalPoints) {
            this.totalPoints = totalPoints;
            this.totalPointsSet = true;
            return this;
        }

        public Builder minCharacteristicValue(int minCharacteristicValue) {
            this.minCharacteristicValue = minCharacteristicValue;
            return this;
        }

        public Builder raceBonusTable(RaceBonusTable raceBonusTable) {
            this.raceBonusTable = raceBonusTable;
            return this;
        }

        public Builder classBonusTable(ClassBonusTable classBonusTable) {
            this.classBonusTable = classBonusTable;
            return this;
        }

        public CharacterResult generate() {
            validateRaceSource();
            validateClassSource();
            validateCharacteristicsSource();
            validateTotalPoints();
            validateMinCharacteristicValue();

            Random random = new Random();
            Race resolvedRace = resolveRace(random);
            logPhase("Resolved race: " + resolvedRace + (randomRace ? " (random)" : " (fixed)"));
            CharacterClass resolvedClass = resolveClass(random);
            logPhase("Resolved class: " + resolvedClass + (randomClass ? " (random)" : " (fixed)"));
            String name = generateName(resolvedRace);
            logPhase("Generated name: " + name);
            List<Characteristic> selectedCharacteristics = resolveCharacteristics();
            logPhase("Selected characteristics: " + selectedCharacteristics);
            validateEnoughPoints(selectedCharacteristics.size());
            logPhase("Points to distribute: " + totalPoints + " (min " + minCharacteristicValue
                    + " per characteristic)");
            List<CharacterCharacteristic> characteristicList = distribute(selectedCharacteristics, random);
            logPhase("After distribution (sum " + sumOf(characteristicList) + "): "
                    + describeCharacteristics(characteristicList));
            characteristicList = applyRaceBonus(resolvedRace, characteristicList);
            characteristicList = applyClassBonus(resolvedClass, characteristicList);
            logPhase("Final character (sum " + sumOf(characteristicList) + "): "
                    + describeCharacteristics(characteristicList));

            return CharacterResult.builder()
                    .race(resolvedRace)
                    .characterClass(resolvedClass)
                    .name(name)
                    .characteristics(characteristicList)
                    .build();
        }

        private String generateName(Race resolvedRace) {
            CharacterNameGeneratorTool.Builder nameBuilder = CharacterNameGeneratorTool.building().race(resolvedRace);
            if (withNickname) {
                nameBuilder.addNickname();
            }
            return nameBuilder.generate().name();
        }

        private List<CharacterCharacteristic> distribute(List<Characteristic> selectedCharacteristics,
                Random random) {
            int count = selectedCharacteristics.size();
            int[] values = new int[count];
            for (int i = 0; i < count; i++) {
                values[i] = minCharacteristicValue;
            }

            int remaining = totalPoints - minCharacteristicValue * count;
            for (int i = 0; i < remaining; i++) {
                values[random.nextInt(count)]++;
            }

            List<CharacterCharacteristic> characteristicList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                characteristicList.add(new CharacterCharacteristic(selectedCharacteristics.get(i), values[i]));
            }
            return characteristicList;
        }

        private List<CharacterCharacteristic> applyRaceBonus(Race race,
                List<CharacterCharacteristic> characteristics) {
            Map<Characteristic, Integer> bonusValueByCharacteristic = raceBonusTable.bonusesFor(race).stream()
                    .collect(Collectors.toMap(RaceBonusTable.CharacteristicBonus::characteristic,
                            RaceBonusTable.CharacteristicBonus::value));

            return applyBonuses("race " + race, bonusValueByCharacteristic, characteristics);
        }

        private List<CharacterCharacteristic> applyClassBonus(CharacterClass characterClass,
                List<CharacterCharacteristic> characteristics) {
            Map<Characteristic, Integer> bonusValueByCharacteristic = classBonusTable.bonusesFor(characterClass)
                    .stream()
                    .collect(Collectors.toMap(ClassBonusTable.CharacteristicBonus::characteristic,
                            ClassBonusTable.CharacteristicBonus::value));

            return applyBonuses("character class " + characterClass, bonusValueByCharacteristic, characteristics);
        }

        private List<CharacterCharacteristic> applyBonuses(String bonusSourceDescription,
                Map<Characteristic, Integer> bonusValueByCharacteristic,
                List<CharacterCharacteristic> characteristics) {
            if (bonusValueByCharacteristic.isEmpty()) {
                logPhase("No bonus from " + bonusSourceDescription);
                return characteristics;
            }

            logPhase("Applying bonus from " + bonusSourceDescription + " (total +"
                    + sumOfBonusValues(bonusValueByCharacteristic) + "): "
                    + describeBonuses(bonusValueByCharacteristic));

            validateAllBonusesTargetKnownCharacteristics(bonusSourceDescription, bonusValueByCharacteristic,
                    characteristics);

            return characteristics.stream()
                    .map(characteristic -> boostCharacteristic(characteristic, bonusValueByCharacteristic))
                    .toList();
        }

        private int sumOfBonusValues(Map<Characteristic, Integer> bonusValueByCharacteristic) {
            return bonusValueByCharacteristic.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        private String describeBonuses(Map<Characteristic, Integer> bonusValueByCharacteristic) {
            return bonusValueByCharacteristic.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> entry.getKey().ordinal()))
                    .map(entry -> entry.getKey() + " +" + entry.getValue())
                    .collect(Collectors.joining(", "));
        }

        private CharacterCharacteristic boostCharacteristic(CharacterCharacteristic characteristic,
                Map<Characteristic, Integer> bonusValueByCharacteristic) {
            int bonusValue = bonusValueByCharacteristic.getOrDefault(characteristic.characteristic(), 0);
            return new CharacterCharacteristic(characteristic.characteristic(), characteristic.value() + bonusValue);
        }

        private void validateAllBonusesTargetKnownCharacteristics(String bonusSourceDescription,
                Map<Characteristic, Integer> bonusValueByCharacteristic,
                List<CharacterCharacteristic> characteristics) {
            Set<Characteristic> presentCharacteristics = characteristics.stream()
                    .map(CharacterCharacteristic::characteristic)
                    .collect(Collectors.toSet());

            Optional<Characteristic> missingCharacteristic = bonusValueByCharacteristic.keySet().stream()
                    .filter(characteristic -> !presentCharacteristics.contains(characteristic))
                    .findFirst();

            if (missingCharacteristic.isPresent()) {
                throw new IllegalStateException("Bonus from " + bonusSourceDescription + " targets characteristic "
                        + missingCharacteristic.get()
                        + " which is not present in the generated character's characteristics");
            }
        }

        private void validateRaceSource() {
            int raceSourceCount = countTrue(race != null, randomRace);

            if (raceSourceCount > 1) {
                throw new IllegalStateException("Only one of race or randomRace can be used together");
            }
            if (raceSourceCount == 0) {
                throw new IllegalStateException("Race must be set before generating a character");
            }
        }

        private void validateClassSource() {
            int classSourceCount = countTrue(characterClass != null, randomClass);

            if (classSourceCount > 1) {
                throw new IllegalStateException("Only one of characterClass or randomClass can be used together");
            }
            if (classSourceCount == 0) {
                throw new IllegalStateException("Character class must be set before generating a character");
            }
        }

        private void validateCharacteristicsSource() {
            int characteristicsSourceCount = countTrue(characteristics != null, allCharacteristics);

            if (characteristicsSourceCount > 1) {
                throw new IllegalStateException(
                        "Only one of characteristics or allCharacteristics can be used together");
            }
            if (characteristicsSourceCount == 0) {
                throw new IllegalStateException(
                        "One of characteristics or allCharacteristics must be set before generating a character");
            }
        }

        private void validateTotalPoints() {
            if (!totalPointsSet) {
                throw new IllegalStateException("Total points must be set before generating a character");
            }
        }

        private void validateMinCharacteristicValue() {
            if (minCharacteristicValue < 0) {
                throw new IllegalStateException("Minimum characteristic value must not be negative");
            }
        }

        private void validateEnoughPoints(int count) {
            if (totalPoints < minCharacteristicValue * count) {
                throw new IllegalStateException("Total points (" + totalPoints
                        + ") are not enough to give each of the " + count
                        + " characteristics the minimum value of " + minCharacteristicValue);
            }
        }

        private Race resolveRace(Random random) {
            if (randomRace) {
                Race[] races = Race.values();
                return races[random.nextInt(races.length)];
            }
            return race;
        }

        private CharacterClass resolveClass(Random random) {
            if (randomClass) {
                CharacterClass[] characterClasses = CharacterClass.values();
                return characterClasses[random.nextInt(characterClasses.length)];
            }
            return characterClass;
        }

        private List<Characteristic> resolveCharacteristics() {
            if (allCharacteristics) {
                return List.of(Characteristic.values());
            }

            List<Characteristic> dedupedCharacteristics = new ArrayList<>(new LinkedHashSet<>(characteristics));
            if (dedupedCharacteristics.isEmpty()) {
                throw new IllegalStateException("Characteristics must not be empty");
            }
            return dedupedCharacteristics;
        }

        private static int countTrue(boolean... conditions) {
            int count = 0;
            for (boolean condition : conditions) {
                if (condition) {
                    count++;
                }
            }
            return count;
        }

        private int sumOf(List<CharacterCharacteristic> characteristics) {
            return characteristics.stream()
                    .mapToInt(CharacterCharacteristic::value)
                    .sum();
        }

        private String describeCharacteristics(List<CharacterCharacteristic> characteristics) {
            return characteristics.stream()
                    .map(characteristic -> characteristic.characteristic() + "=" + characteristic.value())
                    .collect(Collectors.joining(", "));
        }

        private void logPhase(String message) {
            if (verbose) {
                System.out.println("[CharacterGenerator] " + message);
            }
        }
    }
}
