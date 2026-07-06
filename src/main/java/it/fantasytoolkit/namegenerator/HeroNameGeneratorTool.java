package it.fantasytoolkit.namegenerator;

import java.util.Random;

import it.fantasytoolkit.core.types.Seed;
import it.fantasytoolkit.core.types.SeedBuilder;
import it.fantasytoolkit.namegenerator.result.NameResult;
import it.fantasytoolkit.namegenerator.tool.NameGeneratorTool;

public class HeroNameGeneratorTool {

    private static final String NAMES_FILE = "/namegenerator/heros_names.txt";
    private static final String ADJECTIVES_FILE = "/namegenerator/hero_suffix.txt";

    private HeroNameGeneratorTool() {
    }

    public static NameResult generateName() {
        Seed seed = SeedBuilder.newSeed().build();
        return generateName(seed);
    }

    public static NameResult generateName(Seed seed) {
        NameGeneratorTool nameGenerator = new NameGeneratorTool(NAMES_FILE);
        NameGeneratorTool epithetGenerator = new NameGeneratorTool(ADJECTIVES_FILE);

        Random random = new Random(seed.value());
        String name = nameGenerator.pick(random);
        String epithet = epithetGenerator.pick(random);

        return NameResult.builder()
                .name(name + " " + epithet)
                .seed(seed)
                .build();
    }
}
