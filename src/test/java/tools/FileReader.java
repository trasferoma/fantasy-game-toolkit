package tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FileReader {
    public Set<String> readLines(String resourcePath) {
        InputStream in = getClass().getResourceAsStream(resourcePath);
        assertThat(in).as("Resource not found: " + resourcePath).isNotNull();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException("Error reading " + resourcePath, e);
        }
    }
}
