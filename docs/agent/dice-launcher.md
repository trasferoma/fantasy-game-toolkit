# dice-launcher — lancio di gruppi di dadi

Package: `it.fantasytoolkit.dicelauncher`.

Lancia uno o più gruppi di dadi eterogenei e restituisce i risultati
strutturati. È l'**unico tool che chiude con `roll()` invece di `generate()`**:
non genera un elemento di dominio ma un tiro di dadi.

## `DiceLauncherTool`

Entry-point: `DiceLauncherTool.building()` → `Builder` → `roll()` →
`DiceRollResult`.

### Builder

| Metodo | Effetto |
|--------|---------|
| `dice(int numberOfDice, int numberOfFaces)` | aggiunge un gruppo di dadi (es. `2, 6` = `2d6`) |
| `dice(int numberOfDice, int numberOfFaces, String code)` | come sopra con codice/etichetta opzionale |
| `roll()` | chiusura → `DiceRollResult` |

`dice(...)` è **ripetibile** per accumulare gruppi eterogenei (es. `2d6` + `1d20`).

### Comportamento

- Ogni faccia è pescata in `[1, numberOfFaces]`.
- Il `subtotal` di un gruppo è la somma dei suoi dadi.
- Il `total` del result è la somma di tutti i subtotali.

### Risultato

- `DiceRollResult` — `record DiceRollResult(List<DiceRoll> rolls, int total)
  implements GeneratedElementResult`. Builder proprio:
  `DiceRollResult.builder()...build()`.
- `DiceRoll` — `record DiceRoll(int numberOfDice, int numberOfFaces, String code,
  List<Integer> results, int subtotal)`. **Non** implementa
  `GeneratedElementResult`: è un elemento interno al result.

### Errori e vincoli

- Validazione **immediata** in `dice(...)` (`IllegalArgumentException`):
  - `numberOfDice < 1` → "numberOfDice must be at least 1";
  - `numberOfFaces < 2` → "numberOfFaces must be at least 2".
- `roll()` senza alcun gruppo → `IllegalStateException`
  ("At least one dice group must be added before rolling").

### Esempio

```java
DiceRollResult result = DiceLauncherTool.building()
        .dice(2, 6)
        .dice(1, 20, "attacco")
        .roll();

int total = result.total();          // somma di tutti i dadi
List<DiceRoll> rolls = result.rolls();
```
