package it.fantasytoolkit.namegenerator;

import it.fantasytoolkit.core.model.Race;
import it.fantasytoolkit.namegenerator.tool.NameGeneratorTool;

public class EnemyNameGeneratorTool {

    private EnemyNameGeneratorTool() {
    }

    public static String generateName(Race race) {
        NameGeneratorTool nameGenerator = new NameGeneratorTool(race.getNamesFile());
        return nameGenerator.generateName();
    }
}
