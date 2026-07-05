package it.fantasytoolkit.core.types;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeedTest {

    @Test
    void constructorExposesValue() {
        Seed seed = new Seed(123L);

        assertThat(seed.value()).isEqualTo(123L);
    }

    @Test
    void toStringReturnsDecimalRepresentation() {
        Seed seed = new Seed(42L);

        assertThat(seed.value()).isEqualTo(42);
    }

    @Test
    void sameSeedProducesSameRandomSequence() {
        Seed seed = new Seed(987654321L);
        int extractionCount = 20;
        int bound = 1000;

        List<Integer> firstSequence = extractInts(new Random(seed.value()), extractionCount, bound);
        List<Integer> secondSequence = extractInts(new Random(seed.value()), extractionCount, bound);

        assertThat(firstSequence).isEqualTo(secondSequence);
    }

    private List<Integer> extractInts(Random random, int count, int bound) {
        return IntStream.range(0, count)
                .map(i -> random.nextInt(bound))
                .boxed()
                .collect(Collectors.toList());
    }
}
