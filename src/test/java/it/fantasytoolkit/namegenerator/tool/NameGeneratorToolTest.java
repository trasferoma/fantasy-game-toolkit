package it.fantasytoolkit.namegenerator.tool;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NameGeneratorToolTest {

    @Test
    void throwsWhenNamesFileIsMissing() {
        assertThatThrownBy(() -> new NameGeneratorTool("/namegenerator/does_not_exist.txt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Names file not found");
    }

    @Test
    void throwsWhenNameListIsEmpty() {
        NameGeneratorTool nameGenerator = new NameGeneratorTool("/namegenerator/empty_names.txt");

        assertThatThrownBy(() -> nameGenerator.pick(new Random()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Name list not loaded correctly");
    }
}
