package it.fantasytoolkit.dungeongenerator.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.fantasytoolkit.dungeongenerator.result.Chamber;
import it.fantasytoolkit.dungeongenerator.result.ChamberConnection;
import it.fantasytoolkit.dungeongenerator.result.DungeonResult;
import it.fantasytoolkitcore.core.model.ChamberType;

public final class DungeonAsciiRenderer {

    private static final int ROOM_INNER_WIDTH = 10;
    private static final int ROOM_INNER_HEIGHT = 4;
    private static final int ROOM_BOX_WIDTH = ROOM_INNER_WIDTH + 2;
    private static final int ROOM_BOX_HEIGHT = ROOM_INNER_HEIGHT + 2;
    private static final int HORIZONTAL_GUTTER = 4;
    private static final int VERTICAL_GUTTER = 2;
    private static final int CELL_WIDTH = ROOM_BOX_WIDTH + HORIZONTAL_GUTTER;
    private static final int CELL_HEIGHT = ROOM_BOX_HEIGHT + VERTICAL_GUTTER;

    private static final char WALL = '#';
    private static final char FLOOR = '.';
    private static final char CORRIDOR = '#';
    private static final char ENTRY_GLYPH = '<';
    private static final char FINAL_GLYPH = '>';
    private static final char EMPTY = ' ';

    private static final String LEGEND =
            "Legenda: < ingresso  > finale  !N main event  eN nemici  ^N trappole  #<id> id stanza";

    private DungeonAsciiRenderer() {
    }

    public static String render(DungeonResult dungeon) {
        if (dungeon == null) {
            throw new IllegalArgumentException("dungeon must not be null");
        }

        Map<Integer, Integer> columnByChamberId = computeColumns(dungeon);
        Map<Integer, RoomBox> positionByChamberId = computeRoomPositions(dungeon, columnByChamberId);

        Canvas canvas = new Canvas(canvasWidth(positionByChamberId), canvasHeight(positionByChamberId));
        drawRooms(canvas, dungeon, positionByChamberId);
        drawCorridors(canvas, dungeon, positionByChamberId);

        return LEGEND + System.lineSeparator() + System.lineSeparator() + canvas.toText();
    }

    private static Map<Integer, Integer> computeColumns(DungeonResult dungeon) {
        Map<Integer, Set<Integer>> adjacency = buildAdjacency(dungeon);
        int entryChamberId = resolveEntryChamberId(dungeon);

        Map<Integer, Integer> columnByChamberId = new HashMap<>();
        columnByChamberId.put(entryChamberId, 0);

        Deque<Integer> toVisit = new ArrayDeque<>();
        toVisit.add(entryChamberId);

        while (!toVisit.isEmpty()) {
            int chamberId = toVisit.remove();
            int currentColumn = columnByChamberId.get(chamberId);
            for (int neighbourId : adjacency.getOrDefault(chamberId, Set.of())) {
                if (!columnByChamberId.containsKey(neighbourId)) {
                    columnByChamberId.put(neighbourId, currentColumn + 1);
                    toVisit.add(neighbourId);
                }
            }
        }

        for (Chamber chamber : dungeon.chambers()) {
            columnByChamberId.putIfAbsent(chamber.id(), 0);
        }
        return columnByChamberId;
    }

    private static int resolveEntryChamberId(DungeonResult dungeon) {
        return dungeon.chambers().stream()
                .filter(chamber -> chamber.type() == ChamberType.ENTRY)
                .map(Chamber::id)
                .findFirst()
                .orElse(0);
    }

    private static Map<Integer, Set<Integer>> buildAdjacency(DungeonResult dungeon) {
        Map<Integer, Set<Integer>> adjacency = new HashMap<>();
        for (ChamberConnection connection : dungeon.connections()) {
            adjacency.computeIfAbsent(connection.fromChamberId(), id -> new HashSet<>())
                    .add(connection.toChamberId());
            adjacency.computeIfAbsent(connection.toChamberId(), id -> new HashSet<>())
                    .add(connection.fromChamberId());
        }
        return adjacency;
    }

    private static Map<Integer, RoomBox> computeRoomPositions(DungeonResult dungeon,
            Map<Integer, Integer> columnByChamberId) {
        Map<Integer, List<Integer>> chamberIdsByColumn = dungeon.chambers().stream()
                .collect(Collectors.groupingBy(chamber -> columnByChamberId.get(chamber.id()),
                        Collectors.mapping(Chamber::id, Collectors.toList())));

        Map<Integer, RoomBox> positionByChamberId = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : chamberIdsByColumn.entrySet()) {
            int column = entry.getKey();
            List<Integer> chamberIds = entry.getValue();
            chamberIds.sort(Integer::compareTo);
            for (int row = 0; row < chamberIds.size(); row++) {
                positionByChamberId.put(chamberIds.get(row), new RoomBox(column * CELL_WIDTH, row * CELL_HEIGHT));
            }
        }
        return positionByChamberId;
    }

    private static int canvasWidth(Map<Integer, RoomBox> positionByChamberId) {
        int maxColumnX = positionByChamberId.values().stream()
                .mapToInt(RoomBox::x)
                .max()
                .orElse(0);
        return maxColumnX + CELL_WIDTH;
    }

    private static int canvasHeight(Map<Integer, RoomBox> positionByChamberId) {
        int maxRowY = positionByChamberId.values().stream()
                .mapToInt(RoomBox::y)
                .max()
                .orElse(0);
        return maxRowY + CELL_HEIGHT;
    }

    private static void drawRooms(Canvas canvas, DungeonResult dungeon, Map<Integer, RoomBox> positionByChamberId) {
        for (Chamber chamber : dungeon.chambers()) {
            drawRoom(canvas, positionByChamberId.get(chamber.id()), chamber);
        }
    }

    private static void drawRoom(Canvas canvas, RoomBox box, Chamber chamber) {
        drawWalls(canvas, box);
        fillFloor(canvas, box);
        canvas.write(box.interiorLeft(), box.interiorTop(), roomIdLabel(chamber.id()), ROOM_INNER_WIDTH);
        canvas.set(box.centerX(), box.centerY(), centerGlyph(chamber.type()));
        canvas.write(box.interiorLeft(), box.interiorBottom(), statsLabel(chamber), ROOM_INNER_WIDTH);
    }

    private static String roomIdLabel(int chamberId) {
        return "#" + chamberId;
    }

    private static char centerGlyph(ChamberType type) {
        if (type == ChamberType.ENTRY) {
            return ENTRY_GLYPH;
        }
        if (type == ChamberType.FINAL) {
            return FINAL_GLYPH;
        }
        return FLOOR;
    }

    private static String statsLabel(Chamber chamber) {
        List<String> parts = new ArrayList<>();
        if (!chamber.mainEvents().isEmpty()) {
            parts.add("!" + chamber.mainEvents().size());
        }
        if (chamber.enemyCount() > 0) {
            parts.add("e" + chamber.enemyCount());
        }
        if (chamber.trapCount() > 0) {
            parts.add("^" + chamber.trapCount());
        }
        return String.join(" ", parts);
    }

    private static void drawWalls(Canvas canvas, RoomBox box) {
        int left = box.x();
        int top = box.y();
        int right = box.x() + ROOM_BOX_WIDTH - 1;
        int bottom = box.y() + ROOM_BOX_HEIGHT - 1;

        for (int x = left; x <= right; x++) {
            canvas.set(x, top, WALL);
            canvas.set(x, bottom, WALL);
        }
        for (int y = top; y <= bottom; y++) {
            canvas.set(left, y, WALL);
            canvas.set(right, y, WALL);
        }
    }

    private static void fillFloor(Canvas canvas, RoomBox box) {
        for (int y = box.interiorTop(); y <= box.interiorBottom(); y++) {
            for (int x = box.interiorLeft(); x <= box.interiorRight(); x++) {
                canvas.set(x, y, FLOOR);
            }
        }
    }

    private static void drawCorridors(Canvas canvas, DungeonResult dungeon,
            Map<Integer, RoomBox> positionByChamberId) {
        Collection<RoomBox> rooms = positionByChamberId.values();
        for (ChamberConnection connection : dungeon.connections()) {
            RoomBox from = positionByChamberId.get(connection.fromChamberId());
            RoomBox to = positionByChamberId.get(connection.toChamberId());
            drawCorridor(canvas, rooms, from, to);
        }
    }

    private static void drawCorridor(Canvas canvas, Collection<RoomBox> rooms, RoomBox from, RoomBox to) {
        int fromX = from.centerX();
        int fromY = from.centerY();
        int toX = to.centerX();
        int toY = to.centerY();

        drawHorizontalCorridor(canvas, rooms, fromX, toX, fromY);
        drawVerticalCorridor(canvas, rooms, fromY, toY, toX);
    }

    private static void drawHorizontalCorridor(Canvas canvas, Collection<RoomBox> rooms, int fromX, int toX, int y) {
        int start = Math.min(fromX, toX);
        int end = Math.max(fromX, toX);
        for (int x = start; x <= end; x++) {
            drawCorridorCell(canvas, rooms, x, y);
        }
    }

    private static void drawVerticalCorridor(Canvas canvas, Collection<RoomBox> rooms, int fromY, int toY, int x) {
        int start = Math.min(fromY, toY);
        int end = Math.max(fromY, toY);
        for (int y = start; y <= end; y++) {
            drawCorridorCell(canvas, rooms, x, y);
        }
    }

    private static void drawCorridorCell(Canvas canvas, Collection<RoomBox> rooms, int x, int y) {
        if (isRoomInteriorFloor(rooms, x, y)) {
            return;
        }
        canvas.set(x, y, CORRIDOR);
    }

    private static boolean isRoomInteriorFloor(Collection<RoomBox> rooms, int x, int y) {
        return rooms.stream().anyMatch(room -> room.containsInterior(x, y));
    }

    private record RoomBox(int x, int y) {

        private int centerX() {
            return x + 1 + ROOM_INNER_WIDTH / 2;
        }

        private int centerY() {
            return y + 1 + ROOM_INNER_HEIGHT / 2;
        }

        private int interiorLeft() {
            return x + 1;
        }

        private int interiorTop() {
            return y + 1;
        }

        private int interiorRight() {
            return x + ROOM_INNER_WIDTH;
        }

        private int interiorBottom() {
            return y + ROOM_INNER_HEIGHT;
        }

        private boolean containsInterior(int px, int py) {
            return px >= interiorLeft() && px <= interiorRight()
                    && py >= interiorTop() && py <= interiorBottom();
        }
    }

    private static final class Canvas {

        private final char[][] grid;
        private final int width;
        private final int height;

        private Canvas(int width, int height) {
            this.width = width;
            this.height = height;
            this.grid = new char[height][width];
            for (char[] row : grid) {
                Arrays.fill(row, EMPTY);
            }
        }

        private void set(int x, int y, char character) {
            if (isWithinBounds(x, y)) {
                grid[y][x] = character;
            }
        }

        private void write(int x, int y, String text, int maxWidth) {
            String truncated = truncate(text, maxWidth);
            for (int i = 0; i < truncated.length(); i++) {
                set(x + i, y, truncated.charAt(i));
            }
        }

        private String truncate(String text, int maxWidth) {
            return text.length() > maxWidth ? text.substring(0, maxWidth) : text;
        }

        private boolean isWithinBounds(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        private String toText() {
            return IntStream.range(0, height)
                    .mapToObj(y -> rightTrim(new String(grid[y])))
                    .collect(Collectors.joining(System.lineSeparator()));
        }

        private String rightTrim(String line) {
            int end = line.length();
            while (end > 0 && line.charAt(end - 1) == EMPTY) {
                end--;
            }
            return line.substring(0, end);
        }
    }
}
