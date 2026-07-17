package it.fantasytoolkit.dungeongenerator.result;

import java.util.List;

import it.fantasytoolkitcore.core.model.ChamberType;

public record Chamber(int id, ChamberType type, List<MainEvent> mainEvents, int enemyCount, int trapCount) {

    public Chamber(int id, ChamberType type, List<MainEvent> mainEvents, int enemyCount, int trapCount) {
        this.id = id;
        this.type = type;
        this.mainEvents = List.copyOf(mainEvents);
        this.enemyCount = enemyCount;
        this.trapCount = trapCount;
    }
}
