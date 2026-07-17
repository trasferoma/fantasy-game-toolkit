package it.fantasytoolkit.dungeongenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import it.fantasytoolkitcore.core.model.ChamberType;
import it.fantasytoolkit.dungeongenerator.result.Chamber;
import it.fantasytoolkit.dungeongenerator.result.ChamberConnection;
import it.fantasytoolkit.dungeongenerator.result.DungeonResult;
import it.fantasytoolkit.dungeongenerator.result.MainEvent;

public final class DungeonGenerationTool {

    private DungeonGenerationTool() {
    }

    public static Builder building() {
        return new Builder();
    }

    public static final class Builder {

        private static final int MINIMUM_CHAMBERS = 2;
        private static final String MAIN_EVENT_PREFIX = "MainEvent_";

        private int numberOfChambers;
        private boolean numberOfChambersSet;
        private final List<String> finalMainEventCodes = new ArrayList<>();
        private final List<String> randomPositionMainEventCodes = new ArrayList<>();
        private int mainEventCounter;
        private boolean haveTraps;
        private int numberOfEnemy;

        private Builder() {
        }

        public Builder numberOfChambers(int numberOfChambers) {
            this.numberOfChambers = numberOfChambers;
            this.numberOfChambersSet = true;
            return this;
        }

        public Builder mainEvent() {
            finalMainEventCodes.add(nextMainEventCode());
            return this;
        }

        public Builder mainEvent(String code) {
            finalMainEventCodes.add(code);
            return this;
        }

        public Builder randomPositionMainEvent() {
            randomPositionMainEventCodes.add(nextMainEventCode());
            return this;
        }

        public Builder randomPositionMainEvent(String code) {
            randomPositionMainEventCodes.add(code);
            return this;
        }

        public Builder haveTraps() {
            this.haveTraps = true;
            return this;
        }

        public Builder numberOfEnemy(int numberOfEnemy) {
            this.numberOfEnemy = numberOfEnemy;
            return this;
        }

        public DungeonResult generate() {
            validateNumberOfChambers();
            validateNumberOfEnemy();

            Random random = new Random();
            List<List<MainEvent>> mainEventsByChamber = buildEmptyMainEventLists();
            placeFinalMainEvents(mainEventsByChamber);
            placeRandomPositionMainEvents(mainEventsByChamber, random);
            int[] enemyCounts = distributeUniformly(numberOfEnemy, random);
            int numberOfTraps = resolveNumberOfTraps(random);
            int[] trapCounts = distributeUniformly(numberOfTraps, random);
            List<Chamber> chambers = buildChambers(mainEventsByChamber, enemyCounts, trapCounts);
            List<ChamberConnection> connections = buildConnections(random);

            return DungeonResult.builder()
                    .numberOfChambers(numberOfChambers)
                    .chambers(chambers)
                    .connections(connections)
                    .numberOfEnemies(numberOfEnemy)
                    .numberOfTraps(numberOfTraps)
                    .build();
        }

        private String nextMainEventCode() {
            mainEventCounter++;
            return MAIN_EVENT_PREFIX + mainEventCounter;
        }

        private List<List<MainEvent>> buildEmptyMainEventLists() {
            List<List<MainEvent>> mainEventsByChamber = new ArrayList<>();
            for (int chamberId = 0; chamberId < numberOfChambers; chamberId++) {
                mainEventsByChamber.add(new ArrayList<>());
            }
            return mainEventsByChamber;
        }

        private void placeFinalMainEvents(List<List<MainEvent>> mainEventsByChamber) {
            int finalChamberId = numberOfChambers - 1;
            for (String code : finalMainEventCodes) {
                mainEventsByChamber.get(finalChamberId).add(new MainEvent(code));
            }
        }

        private void placeRandomPositionMainEvents(List<List<MainEvent>> mainEventsByChamber, Random random) {
            for (String code : randomPositionMainEventCodes) {
                int chamberId = 1 + random.nextInt(numberOfChambers - 1);
                mainEventsByChamber.get(chamberId).add(new MainEvent(code));
            }
        }

        private int[] distributeUniformly(int total, Random random) {
            int[] counts = new int[numberOfChambers];
            int base = total / numberOfChambers;
            int remainder = total % numberOfChambers;

            for (int chamberId = 0; chamberId < numberOfChambers; chamberId++) {
                counts[chamberId] = base;
            }

            List<Integer> shuffledChamberIds = shuffledChamberIds(random);
            for (int i = 0; i < remainder; i++) {
                counts[shuffledChamberIds.get(i)]++;
            }
            return counts;
        }

        private int resolveNumberOfTraps(Random random) {
            if (!haveTraps) {
                return 0;
            }
            return random.nextInt(numberOfChambers + 1);
        }

        private List<Chamber> buildChambers(List<List<MainEvent>> mainEventsByChamber, int[] enemyCounts,
                int[] trapCounts) {
            List<Chamber> chambers = new ArrayList<>();
            for (int chamberId = 0; chamberId < numberOfChambers; chamberId++) {
                ChamberType type = resolveChamberType(chamberId);
                chambers.add(new Chamber(chamberId, type, mainEventsByChamber.get(chamberId),
                        enemyCounts[chamberId], trapCounts[chamberId]));
            }
            return chambers;
        }

        private ChamberType resolveChamberType(int chamberId) {
            if (chamberId == 0) {
                return ChamberType.ENTRY;
            }
            if (chamberId == numberOfChambers - 1) {
                return ChamberType.FINAL;
            }
            return ChamberType.STANDARD;
        }

        private List<ChamberConnection> buildConnections(Random random) {
            Set<ChamberConnection> connections = new LinkedHashSet<>();
            connectSpanningTree(connections, random);
            addExtraConnections(connections, random);
            return new ArrayList<>(connections);
        }

        private void connectSpanningTree(Set<ChamberConnection> connections, Random random) {
            List<Integer> shuffledChamberIds = shuffledChamberIds(random);
            List<Integer> connectedChamberIds = new ArrayList<>();
            connectedChamberIds.add(shuffledChamberIds.get(0));

            for (int i = 1; i < shuffledChamberIds.size(); i++) {
                int chamberId = shuffledChamberIds.get(i);
                int connectedToChamberId = connectedChamberIds.get(random.nextInt(connectedChamberIds.size()));
                connections.add(new ChamberConnection(chamberId, connectedToChamberId));
                connectedChamberIds.add(chamberId);
            }
        }

        private void addExtraConnections(Set<ChamberConnection> connections, Random random) {
            int extraConnectionCount = random.nextInt(numberOfChambers / 2 + 1);
            for (int i = 0; i < extraConnectionCount; i++) {
                int firstChamberId = random.nextInt(numberOfChambers);
                int secondChamberId = random.nextInt(numberOfChambers);
                if (firstChamberId == secondChamberId) {
                    continue;
                }
                connections.add(new ChamberConnection(firstChamberId, secondChamberId));
            }
        }

        private List<Integer> shuffledChamberIds(Random random) {
            List<Integer> chamberIds = new ArrayList<>();
            for (int chamberId = 0; chamberId < numberOfChambers; chamberId++) {
                chamberIds.add(chamberId);
            }
            Collections.shuffle(chamberIds, random);
            return chamberIds;
        }

        private void validateNumberOfChambers() {
            if (!numberOfChambersSet) {
                throw new IllegalStateException("Number of chambers must be set before generating a dungeon");
            }
            if (numberOfChambers < MINIMUM_CHAMBERS) {
                throw new IllegalStateException(
                        "Number of chambers must be at least 2 (entry chamber and final chamber)");
            }
        }

        private void validateNumberOfEnemy() {
            if (numberOfEnemy < 0) {
                throw new IllegalStateException("Number of enemies must not be negative");
            }
        }
    }
}
