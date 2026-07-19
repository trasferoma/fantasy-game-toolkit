# character-generator — generazione di personaggi

Package: `it.fantasytoolkit.charactergenerator`.

Genera un personaggio con razza, classe, nome e caratteristiche. Non usa rarità
né buff/debuff: distribuisce un monte punti tra le caratteristiche e applica i
bonus di razza e classe.

## `CharacterGeneratorTool`

Entry-point: `CharacterGeneratorTool.building()` → `Builder` → `generate()` →
`CharacterResult`.

### Builder

**Razza** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `race(Race race)` | razza fissa |
| `randomRace()` | razza casuale tra tutti i `Race.values()` |

**Classe** — esattamente **una** fonte (il personaggio ha **sempre** una classe):

| Metodo | Effetto |
|--------|---------|
| `characterClass(CharacterClass characterClass)` | classe fissa |
| `randomClass()` | classe casuale tra tutti i `CharacterClass.values()` |

**Caratteristiche** — esattamente **una** fonte:

| Metodo | Effetto |
|--------|---------|
| `characteristics(List<Characteristic>)` | lista esplicita (de-duplicata preservando l'ordine di inserimento, `LinkedHashSet`) |
| `allCharacteristics()` | tutte le `Characteristic.values()` |

**Punti**:

| Metodo | Obbligo | Default | Effetto |
|--------|---------|---------|---------|
| `totalPoints(int)` | **obbligatorio** | — | monte punti totale distribuito |
| `minCharacteristicValue(int)` | opzionale | `1` | valore minimo garantito a ogni caratteristica (non negativo) |

**Opzionali**:

| Metodo | Default | Effetto |
|--------|---------|---------|
| `addNickname()` | off | aggiunge il soprannome al nome |
| `raceBonusTable(RaceBonusTable)` | `RaceBonusTable.withDefaultBonuses()` | sovrascrive i bonus di razza |
| `classBonusTable(ClassBonusTable)` | `ClassBonusTable.withDefaultBonuses()` | sovrascrive i bonus di classe |
| `verbose()` | off | logga le fasi su `System.out` (prefisso `[CharacterGenerator]`) |

**Chiusura**: `generate()` → `CharacterResult`.

### Comportamento

- **Nome**: sempre generato internamente riusando `CharacterNameGeneratorTool`
  con la razza risolta; `addNickname()` aggiunge il soprannome.
- **Distribuzione**: ogni caratteristica parte dal minimo, poi i punti residui
  (`totalPoints - min * count`) vengono assegnati **uno alla volta a
  caratteristiche casuali**. La somma dopo la sola distribuzione è esattamente
  `totalPoints`.
- **Bonus** (ultimi step, prima razza poi classe): si applicano i bonus additivi
  di `RaceBonusTable` e `ClassBonusTable`. Con i default attivi la somma finale è
  `totalPoints + bonusRazza + bonusClasse`. Una tabella **vuota**
  (`builder().build()`) è l'opt-out dai relativi bonus.
- **Verbose**: puramente osservazionale; con `verbose` off non stampa nulla e il
  comportamento (valori, validazioni, eccezioni) è identico.

### Risultato

- `CharacterResult` — `record CharacterResult(Race race, CharacterClass
  characterClass, String name, List<CharacterCharacteristic> characteristics)
  implements GeneratedElementResult`. Builder proprio:
  `CharacterResult.builder()...build()`.
- `CharacterCharacteristic` — `record CharacterCharacteristic(Characteristic
  characteristic, int value)`.

### Errori e vincoli

Tutti `IllegalStateException`, sollevati in `generate()`:

- nessuna fonte di razza, oppure entrambe;
- nessuna fonte di classe, oppure entrambe;
- nessuna fonte di caratteristiche, oppure entrambe;
- lista di caratteristiche vuota dopo il dedup;
- `totalPoints` non impostato;
- `minCharacteristicValue` negativo;
- `totalPoints < minCharacteristicValue * count` (punti insufficienti per il minimo);
- un bonus (di razza o classe) punta a una caratteristica **non presente** nel
  personaggio generato (il bonus non viene ignorato: è un errore).

> **Conseguenza pratica**: con le tabelle di bonus di default attive non si può
> generare un personaggio con un sottoinsieme di caratteristiche che escluda i
> target dei bonus della razza/classe risolte. Per un sottoinsieme ristretto,
> usare tabelle di bonus vuote (opt-out) o coerenti col sottoinsieme.

### Esempi

```java
CharacterGeneratorTool.building()
        .race(Race.ELF).characterClass(CharacterClass.MAGE)
        .allCharacteristics().totalPoints(30).generate();

CharacterGeneratorTool.building()
        .randomRace().randomClass().addNickname()
        .allCharacteristics().totalPoints(50).generate();

CharacterGeneratorTool.building()
        .race(Race.ORC).characterClass(CharacterClass.WARRIOR)
        .allCharacteristics().minCharacteristicValue(0).totalPoints(10).generate();

// opt-out da entrambi i bonus: sottoinsieme di una sola caratteristica
CharacterGeneratorTool.building()
        .race(Race.HUMAN).characterClass(CharacterClass.THIEF)
        .characteristics(List.of(Characteristic.STRENGTH)).totalPoints(1)
        .raceBonusTable(RaceBonusTable.builder().build())
        .classBonusTable(ClassBonusTable.builder().build())
        .generate();

// logga fasi e bonus su System.out
CharacterGeneratorTool.building()
        .race(Race.ORC).characterClass(CharacterClass.WARRIOR)
        .allCharacteristics().totalPoints(20).verbose().generate();
```
