package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.model.Race;

public class EnemyNameGeneratorTool {

    private EnemyNameGeneratorTool() {
    }

    public static String generateName(Race race) {
        NameGeneratorTool nameGenerator = new NameGeneratorTool(race.getNamesFile());
        return nameGenerator.generateName();
    }
}
