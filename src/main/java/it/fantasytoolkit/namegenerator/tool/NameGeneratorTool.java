package it.fantasytoolkit.namegenerator.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NameGeneratorTool {

    private final List<String> names = new ArrayList<>();
    private final Random random = new Random();

    public NameGeneratorTool(String namesFile) {
        loadNames(namesFile);
    }

    public String generateName() {
        if (names.isEmpty()) {
            throw new IllegalStateException("Name list not loaded correctly");
        }
        return names.get(random.nextInt(names.size()));
    }

    private void loadNames(String resourcePath) {
        try (InputStream in = openResource(resourcePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    names.add(trimmed);
                }
            }
        } catch (IOException e) {
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
