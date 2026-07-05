package it.fantasytoolkit.core.types;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeedBuilderTest {

    @Test
    void buildReturnsUsableSeed() {
        Seed seed = SeedBuilder.newSeed().build();

        assertThat(seed).isNotNull();
        assertThat(seed.toString()).isEqualTo(Long.toString(seed.value()));
    }

    @Test
    void repeatedBuildsAreNotAlwaysIdentical() {
        int buildCount = 20;

        Set<Long> generatedValues = IntStream.range(0, buildCount)
                .mapToObj(i -> SeedBuilder.newSeed().build().value())
                .collect(Collectors.toSet());

        assertThat(generatedValues).as("Repeated SeedBuilder.build() calls must not always collide")
                .hasSizeGreaterThan(1);
    }
}
