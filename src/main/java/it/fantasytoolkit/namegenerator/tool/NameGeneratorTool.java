package it.fantasytoolkit.namegenerator.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class NameGeneratorTool {

    private final List<String> names;

    public NameGeneratorTool(String namesFile) {
        this.names = loadNames(namesFile);
    }

    public String pick(Random random) {
        if (names.isEmpty()) {
            throw new IllegalStateException("Name list not loaded correctly");
        }
        return names.get(random.nextInt(names.size()));
    }

    private static List<String> loadNames(String resourcePath) {
        try (InputStream in = openResource(resourcePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("Unable to load names file: " + resourcePath, e);
        }
    }

    private static InputStream openResource(String resourcePath) {
        InputStream in = NameGeneratorTool.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("Names file not found: " + resourcePath);
        }
        return in;
    }
}
