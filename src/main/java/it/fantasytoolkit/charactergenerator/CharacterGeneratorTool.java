package it.fantasytoolkit.charactergenerator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import it.fantasytoolkitcore.core.model.Characteristic;
import it.fantasytoolkitcore.core.model.Race;
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
        private boolean withNickname;
        private List<Characteristic> characteristics;
        private boolean allCharacteristics;
        private int totalPoints;
        private boolean totalPointsSet;
        private int minCharacteristicValue = 1;

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

        public Builder addNickname() {
            this.withNickname = true;
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

        public CharacterResult generate() {
            validateRaceSource();
            validateCharacteristicsSource();
            validateTotalPoints();
            validateMinCharacteristicValue();

            Random random = new Random();
            Race resolvedRace = resolveRace(random);
            String name = generateName(resolvedRace);
            List<Characteristic> selectedCharacteristics = resolveCharacteristics();
            validateEnoughPoints(selectedCharacteristics.size());
            List<CharacterCharacteristic> characteristicList = distribute(selectedCharacteristics, random);

            return CharacterResult.builder()
                    .race(resolvedRace)
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

        private void validateRaceSource() {
            int raceSourceCount = countTrue(race != null, randomRace);

            if (raceSourceCount > 1) {
                throw new IllegalStateException("Only one of race or randomRace can be used together");
            }
            if (raceSourceCount == 0) {
                throw new IllegalStateException("Race must be set before generating a character");
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
    }
}
