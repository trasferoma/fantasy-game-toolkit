package it.fantasytoolkit.namegenerator;

public class HeroNameGeneratorTool {

    private static final String NAMES_FILE = "/namegenerator/heros_names.txt";
    private static final String ADJECTIVES_FILE = "/namegenerator/hero_suffix.txt";

    private HeroNameGeneratorTool() {
    }

    public static String generateName() {
        NameGeneratorTool nameGenerator = new NameGeneratorTool(NAMES_FILE);
        NameGeneratorTool epithetGenerator = new NameGeneratorTool(ADJECTIVES_FILE);

        String name = nameGenerator.generateName();
        String epithet = epithetGenerator.generateName();

        return name + " " + epithet;
    }
}
