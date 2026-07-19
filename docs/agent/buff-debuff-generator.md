# buff-debuff-generator — generazione di buff/debuff per rarità

Package: `it.fantasytoolkit.buffdebuffgenerator`.

Genera gli status effect (buff/debuff) di un elemento in base alla rarità. È il
componente riusato internamente da jewel, weapon, armour e potion generator per
assegnare i buff.

> **Stato attuale (lacuna nota)**: i **debuff non sono ancora generati** —
> `debuffs` è sempre `List.of()`. La struttura per estenderli esiste
> (`DebuffElement`, `StatusEffect`), ma la logica di produzione va ancora scritta.
> Chi consuma il tool riceve oggi solo buff.

## `BuffDebuffGeneratorTool`

Entry-point: `BuffDebuffGeneratorTool.building()` → `Builder` → `generate()` →
`BuffDebuffResult`.

### Builder

| Metodo | Obbligo | Default | Effetto |
|--------|---------|---------|---------|
| `rarity(Rarity rarity)` | **obbligatorio** | — | rarità che seleziona le combinazioni ammesse |
| `rules(BuffDebuffRules rules)` | opzionale | `DefaultBuffDebuffRules` | regole custom (combinazioni e range) |
| `generate()` | chiusura | — | produce il `BuffDebuffResult` |

### Comportamento

1. la rarità seleziona la lista di `BuffCombination` ammesse;
2. se ne pesca una a caso;
3. ogni combinazione definisce `count` (quanti buff) e l'intervallo
   `[minValue, maxValue]` dei valori;
4. le `Characteristic` vengono mescolate e se ne prendono le prime `count`,
   ciascuna con un valore casuale nell'intervallo.

Ogni rarità produce sempre **almeno un buff** (`count >= 1` in ogni combinazione
di default).

### Risultato — `BuffDebuffResult`

`record BuffDebuffResult(List<BuffElement> buffs, List<DebuffElement> debuffs)
implements GeneratedElementResult`.

- `BuffElement(Characteristic characteristic, int value)` — implementa `StatusEffect`.
- `DebuffElement(Characteristic characteristic, int value)` — implementa `StatusEffect`.
- `StatusEffect` — interfaccia comune: `characteristic()`, `value()`.

### Errori e vincoli

- `generate()` senza `rarity(...)` → `IllegalStateException`
  ("Rarity must be set before generating buffs and debuffs").

### Esempi

```java
BuffDebuffResult result = BuffDebuffGeneratorTool.building()
        .rarity(Rarity.RARE)
        .generate();

List<BuffElement> buffs = result.buffs();   // >= 1 elemento
List<DebuffElement> debuffs = result.debuffs();   // oggi sempre vuota
```

## Regole — punto di estensione

Package `it.fantasytoolkit.buffdebuffgenerator.rules`.

- `BuffCombination(int count, int minValue, int maxValue)` — record combinazione.
- `BuffDebuffRules` — interfaccia: `List<BuffCombination> combinationsFor(Rarity)`.
- `DefaultBuffDebuffRules` — mappa `Rarity` → combinazioni (`EnumMap`).

Passando una `BuffDebuffRules` custom a `rules(...)` si cambiano combinazioni e
range senza toccare il tool.

### Combinazioni di default (`DefaultBuffDebuffRules`)

Ogni combinazione è `(count, minValue, maxValue)`; a ogni `generate()` se ne
pesca una a caso tra quelle della rarità.

| Rarità | Combinazioni ammesse |
|--------|----------------------|
| COMMON | `(1, 1, 2)` |
| UNCOMMON | `(1, 3, 4)`, `(2, 1, 2)` |
| RARE | `(1, 5, 6)`, `(2, 3, 4)`, `(3, 1, 2)` |
| EPIC | `(1, 7, 8)`, `(2, 5, 6)`, `(3, 3, 4)`, `(4, 1, 2)` |
| LEGENDARY | `(1, 9, 10)`, `(2, 7, 8)`, `(3, 5, 6)`, `(4, 3, 4)`, `(5, 1, 2)` |
