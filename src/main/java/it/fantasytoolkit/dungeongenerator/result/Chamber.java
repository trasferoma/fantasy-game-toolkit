package it.fantasytoolkit.dungeongenerator.result;

import java.util.List;

import it.fantasytoolkitcore.core.model.ChamberType;

public record Chamber(int id, ChamberType type, List<MainEvent> mainEvents, int enemyCount, int trapCount,
        int chestCount) {

    public Chamber(int id, ChamberType type, List<MainEvent> mainEvents, int enemyCount, int trapCount,
            int chestCount) {
        this.id = id;
        this.type = type;
        this.mainEvents = List.copyOf(mainEvents);
        this.enemyCount = enemyCount;
        this.trapCount = trapCount;
        this.chestCount = chestCount;
    }
}
