package it.fantasytoolkit.dungeongenerator.result;

import java.util.List;

import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

public record DungeonResult(int numberOfChambers, List<Chamber> chambers, List<ChamberConnection> connections,
        int numberOfEnemies, int numberOfTraps, int numberOfChests) implements GeneratedElementResult {
    public static DungeonResult.Builder builder() {
        return new DungeonResult.Builder();
    }

    public static final class Builder {
        private int numberOfChambers;
        private List<Chamber> chambers;
        private List<ChamberConnection> connections;
        private int numberOfEnemies;
        private int numberOfTraps;
        private int numberOfChests;

        private Builder() {
        }

        public DungeonResult.Builder numberOfChambers(int numberOfChambers) {
            this.numberOfChambers = numberOfChambers;
            return this;
        }

        public DungeonResult.Builder chambers(List<Chamber> chambers) {
            this.chambers = chambers;
            return this;
        }

        public DungeonResult.Builder connections(List<ChamberConnection> connections) {
            this.connections = connections;
            return this;
        }

        public DungeonResult.Builder numberOfEnemies(int numberOfEnemies) {
            this.numberOfEnemies = numberOfEnemies;
            return this;
        }

        public DungeonResult.Builder numberOfTraps(int numberOfTraps) {
            this.numberOfTraps = numberOfTraps;
            return this;
        }

        public DungeonResult.Builder numberOfChests(int numberOfChests) {
            this.numberOfChests = numberOfChests;
            return this;
        }

        public DungeonResult build() {
            return new DungeonResult(numberOfChambers, chambers, connections, numberOfEnemies, numberOfTraps,
                    numberOfChests);
        }
    }
}
