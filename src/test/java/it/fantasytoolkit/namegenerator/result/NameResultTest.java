package it.fantasytoolkit.namegenerator.result;

import it.fantasytoolkit.core.pojo.GeneratedElementResult;
import it.fantasytoolkit.core.types.Seed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameResultTest {

    @Test
    void builderCreatesResultWithNameAndSeed() {
        Seed seed = new Seed(42L);

        NameResult result = NameResult.builder()
                .name("Aragorn")
                .seed(seed)
                .build();

        assertThat(result.name()).isEqualTo("Aragorn");
        assertThat(result.seed()).isEqualTo(seed);
    }

    @Test
    void exposesSeedAsGeneratedElementResultContract() {
        Seed seed = new Seed(7L);

        GeneratedElementResult result = NameResult.builder()
                .name("Legolas")
                .seed(seed)
                .build();

        assertThat(result.seed()).isEqualTo(seed);
    }
}
