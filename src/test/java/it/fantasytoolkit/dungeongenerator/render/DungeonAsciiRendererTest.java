package it.fantasytoolkit.dungeongenerator.render;

import java.util.List;

import it.fantasytoolkit.dungeongenerator.DungeonGenerationTool;
import it.fantasytoolkit.dungeongenerator.result.DungeonResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DungeonAsciiRendererTest {

    private static final int ITERATIONS = 20;

    @Test
    void printsAVisualMapForAVariedDungeon() {
        DungeonResult result = DungeonGenerationTool.building()
                .numberOfChambers(8)
                .mainEvent()
                .mainEvent()
                .randomPositionMainEvent()
                .haveTraps()
                .numberOfEnemy(9)
                .generate();

        String map = DungeonAsciiRenderer.render(result);

        System.out.println(map);

        assertThat(map).isNotBlank();
    }

    @Test
    void rendersEntryAndFinalGlyphs() {
        DungeonResult result = DungeonGenerationTool.building()
                .numberOfChambers(8)
                .generate();

        String map = DungeonAsciiRenderer.render(result);
        System.out.println(map);

        assertThat(map).contains("<");
        assertThat(map).contains(">");
    }

    @Test
    void rendersWallsAndFloors() {
        DungeonResult result = DungeonGenerationTool.building()
                .numberOfChambers(8)
                .generate();

        String map = DungeonAsciiRenderer.render(result);

        assertThat(map).contains("#");
        assertThat(map).contains(".");
    }

    @Test
    void rendersEntryChamberId() {
        DungeonResult result = DungeonGenerationTool.building()
                .numberOfChambers(8)
                .generate();

        String map = DungeonAsciiRenderer.render(result);

        assertThat(map).contains("#0");
    }

    @Test
    void rendersLegendAsideTheMap() {
        DungeonResult result = DungeonGenerationTool.building()
                .numberOfChambers(8)
                .numberOfChests(5)
                .generate();

        String map = DungeonAsciiRenderer.render(result);

        assertThat(map).contains("Legenda:");
        assertThat(map).contains("< ingresso");
        assertThat(map).contains("> finale");
        assertThat(map).contains("!N main event");
        assertThat(map).contains("eN nemici");
        assertThat(map).contains("^N trappole");
        assertThat(map).contains("$N scrigni");
        assertThat(map).contains("#<id> id stanza");

        System.out.println(map);
    }

    @Test
    void noLineHasTrailingWhitespace() {
        DungeonResult result = DungeonGenerationTool.building()
                .numberOfChambers(8)
                .mainEvent()
                .randomPositionMainEvent()
                .haveTraps()
                .numberOfEnemy(5)
                .generate();

        String map = DungeonAsciiRenderer.render(result);

        List<String> lines = List.of(map.split("\\R", -1));
        assertThat(lines).allMatch(line -> line.equals(line.stripTrailing()));
    }

    @Test
    void rendersMinimalDungeonWithoutErrors() {
        for (int i = 0; i < ITERATIONS; i++) {
            DungeonResult result = DungeonGenerationTool.building()
                    .numberOfChambers(2)
                    .generate();

            assertThat(DungeonAsciiRenderer.render(result)).isNotBlank();
        }
    }

    @Test
    void rendersLargeDungeonWithoutErrors() {
        for (int i = 0; i < ITERATIONS; i++) {
            DungeonResult result = DungeonGenerationTool.building()
                    .numberOfChambers(15)
                    .mainEvent()
                    .randomPositionMainEvent()
                    .haveTraps()
                    .numberOfEnemy(30)
                    .generate();

            assertThat(DungeonAsciiRenderer.render(result)).isNotBlank();
        }
    }

    @Test
    void renderWithNullDungeonThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> DungeonAsciiRenderer.render(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("dungeon must not be null");
    }
}
