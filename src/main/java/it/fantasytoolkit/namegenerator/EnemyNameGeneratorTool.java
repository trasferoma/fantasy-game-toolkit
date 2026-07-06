package it.fantasytoolkit.namegenerator;

import java.util.Random;

import it.fantasytoolkit.core.model.Race;
import it.fantasytoolkit.core.types.Seed;
import it.fantasytoolkit.core.types.SeedBuilder;
import it.fantasytoolkit.namegenerator.result.NameResult;
import it.fantasytoolkit.namegenerator.tool.NameGeneratorTool;

public class EnemyNameGeneratorTool {

    private EnemyNameGeneratorTool() {
    }

    public static NameResult generateName(Race race) {
        Seed seed = SeedBuilder.newSeed().build();
        return generateName(race, seed);
    }

    public static NameResult generateName(Race race, Seed seed) {
        NameGeneratorTool nameGenerator = new NameGeneratorTool(race.getNamesFile());
        Random random = new Random(seed.value());
        String name = nameGenerator.pick(random);

        return NameResult.builder()
                .name(name)
                .seed(seed)
                .build();
    }
}
