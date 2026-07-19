# dungeon-generator — generazione di dungeon a stanze collegate

Package: `it.fantasytoolkit.dungeongenerator`.

Costruisce una mappa di stanze connesse e vi distribuisce eventi principali,
nemici, trappole e scrigni. Non usa rarità né buff/debuff. Include un renderer
ASCII (`DungeonAsciiRenderer`), che **non** è un `*Tool`.

## `DungeonGenerationTool`

Entry-point: `DungeonGenerationTool.building()` → `Builder` → `generate()` →
`DungeonResult`.

### Builder

| Metodo | Obbligo | Default | Effetto |
|--------|---------|---------|---------|
| `numberOfChambers(int)` | **obbligatorio** | — | numero di stanze, minimo `2` (ingresso + finale) |
| `mainEvent()` | opzionale (ripetibile) | — | evento principale con codice automatico `MainEvent_<n>`, **nella stanza finale** |
| `mainEvent(String code)` | opzionale (ripetibile) | — | come sopra, con codice esplicito |
| `randomPositionMainEvent()` | opzionale (ripetibile) | — | evento con codice automatico, in una stanza casuale **diversa dall'ingresso** |
| `randomPositionMainEvent(String code)` | opzionale (ripetibile) | — | come sopra, con codice esplicito |
| `numberOfEnemy(int)` | opzionale | `0` | nemici totali, distribuiti uniformemente (negativo → errore) |
| `numberOfChests(int)` | opzionale | `0` | scrigni totali, distribuiti uniformemente (negativo → errore) |
| `haveTraps()` | opzionale | off | abilita trappole: numero casuale in `[0, numberOfChambers]`, distribuito uniformemente |
| `generate()` | chiusura | — | produce il `DungeonResult` |

### Comportamento

- **Tipi di stanza**: stanza `0` sempre `ENTRY`, ultima `FINAL`, intermedie
  `STANDARD`.
- **Eventi principali**: i `mainEvent*` finiscono **sempre nella stanza finale**;
  i `randomPositionMainEvent*` in una stanza casuale di indice
  `1..numberOfChambers-1`. Il codice automatico usa un contatore progressivo
  condiviso (`MainEvent_1`, `MainEvent_2`, ...).
- **Distribuzione uniforme** (nemici, scrigni, trappole): base uguale per tutte
  le stanze, il resto va a stanze casuali.
- **Connessioni**: prima uno **spanning tree** casuale che garantisce la
  raggiungibilità di ogni stanza, poi fino a `numberOfChambers/2` connessioni
  extra casuali. Ogni `ChamberConnection` è normalizzata (`from = min`,
  `to = max`) e deduplicata: A→B e B→A sono la stessa connessione.

### Risultato

- `DungeonResult` — `record DungeonResult(int numberOfChambers,
  List<Chamber> chambers, List<ChamberConnection> connections,
  int numberOfEnemies, int numberOfTraps, int numberOfChests) implements
  GeneratedElementResult`. Builder proprio: `DungeonResult.builder()...build()`.
- `Chamber` — `record Chamber(int id, ChamberType type, List<MainEvent>
  mainEvents, int enemyCount, int trapCount, int chestCount)` (difensivo:
  `List.copyOf` sui `mainEvents`).
- `ChamberConnection` — `record ChamberConnection(int fromChamberId,
  int toChamberId)`; il compact constructor ordina gli id (`min`/`max`).
- `MainEvent` — `record MainEvent(String code)`.

### Errori e vincoli

Tutti `IllegalStateException`, sollevati in `generate()`:

- `numberOfChambers` non impostato;
- `numberOfChambers < 2`;
- `numberOfEnemy` negativo;
- `numberOfChests` negativo.

### Esempio

```java
DungeonResult dungeon = DungeonGenerationTool.building()
        .numberOfChambers(6)
        .mainEvent().randomPositionMainEvent()
        .numberOfEnemy(10).numberOfChests(3).haveTraps()
        .generate();
```

## `dungeongenerator.render.DungeonAsciiRenderer`

Renderer statico (non un `*Tool`): `render(DungeonResult)` → `String`.

Disegna le stanze come box ASCII con corridoi, glifi `<`/`>` per
ingresso/finale, e un'etichetta di stats per stanza. Affianca alla mappa una
legenda in colonna.

**Glifi e legenda**:

| Glifo | Significato |
|-------|-------------|
| `<` | stanza d'ingresso |
| `>` | stanza finale |
| `!N` | N eventi principali |
| `eN` | N nemici |
| `^N` | N trappole |
| `$N` | N scrigni |
| `#<id>` | id della stanza |

**Errori**: `render(null)` → `IllegalArgumentException`.

```java
String map = DungeonAsciiRenderer.render(dungeon);
System.out.println(map);
```
