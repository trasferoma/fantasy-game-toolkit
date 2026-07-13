# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandi

Progetto Maven, **Java 21**. La macchina ha come default un JDK 17 (`JAVA_HOME` → `C:\lavoro\jdk\openlogic-openjdk-17...`), che **non compila** (`invalid target release: 21`). Per i comandi Maven punta `JAVA_HOME` al JDK 21:

```bash
export JAVA_HOME="/c/Users/fabio.dearcangelis/.jdks/ms-21.0.9"   # git bash

mvn test                                        # compila ed esegue tutti i test
mvn test -Dtest=CharacterNameGeneratorToolTest  # una singola classe di test
mvn test -Dtest=CharacterNameGeneratorToolTest#generatesElfNameFromDictionaryWithoutNickname  # un singolo metodo
mvn compile                                # sola compilazione
mvn package                                # build completa
```

Test: JUnit Jupiter (JUnit 5) + AssertJ (`assertThat(...)`). Surefire con `useModulePath=false`. Lombok è `provided` (`@Getter` risolto a compile-time).

## Architettura

Toolkit per giochi fantasy, organizzato in **moduli logici (package)** dentro un singolo modulo Maven. Ogni generatore pubblico è un `*Tool` con costruttore privato e un entry-point statico `building()` che stacca un `Builder` interno fluente; la catena si chiude sempre con `generate()`, che restituisce un result immutabile (record). Ogni result implementa `GeneratedElementResult`, oggi **marker interface vuota** (`core.pojo.GeneratedElementResult`): un contratto-tag comune tra i result, senza metodi.

I `*Tool` usano internamente `new Random()`: la generazione è **casuale e non riproducibile** (nessun seme esposto).

### `core` — modello di dominio e tipi condivisi

Package `it.fantasytoolkitcore.core` (nota: distinto dal `groupId` `it.fantasytoolkit` e dal package dei tool).

- `core.model.Race` — enum (`HUMAN`/`ELF`/`ORC`/`UNDEAD`), `@Getter` Lombok, mappa ogni razza al proprio file di nomi (`namesFile`) e a un `symbol` char. Punto di estensione per nuove razze: nuova costante + relativo file di risorse.
- `core.model.Rarity` — enum ordinato dal meno al più raro: `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`. L'ordine (`ordinal()`) è semanticamente rilevante (vedi `maxRarity`).
- `core.model.Jewel` — enum dei tipi di gioiello: `RING`, `NECKLACE`, `BRACELET`, `EARRING`.
- `core.model.Weapon` — enum dei tipi di arma: `SWORD`, `BOW`, `AXE`, `HAMMER`, `SHIELD`, `DAGGER`, `CROSSBOW`, `BATTLEAXE`, `STAFF`, `SCEPTER`.
- `core.model.Armour` — enum dei tipi di armatura: `CHESTPLATE`, `PANTS`, `BOOTS`, `GAUNTLETS`, `HELMET`, `BELT`.
- `core.model.Characteristic` — enum delle caratteristiche di gioco: `STRENGTH`, `INTELLIGENCE`, `AGILITY`, `WISDOM`, `CHARISMA`, `RESISTANCE`, `STAMINA`, `LUCK`.
- `core.model.RarityTable` — tabella di estrazione pesata delle rarità. Costruita con `RarityTable.builder().entry(rarity, weight)...build()`; `draw(Random)` restituisce una `Rarity` con probabilità proporzionale al peso. `build()` valida che i pesi siano positivi, le rarità uniche e la **somma dei pesi pari a 100**; altrimenti `IllegalStateException`. Immutabile (`List.copyOf`).
- `core.pojo.GeneratedElementResult` — marker interface comune a tutti i result.

### `namegenerator` — generazione di nomi da liste di parole del classpath

- `namegenerator.tool.NameGeneratorTool` — motore base (costruttore pubblico che riceve il path del file). Carica un file dal classpath (una parola per riga, UTF-8, righe vuote scartate). L'estrazione avviene con `pick(Random)`, **unico punto di selezione**, con un `Random` fornito dall'esterno. Risorsa mancante o lista vuota → `IllegalStateException` con messaggio diagnostico. È il mattone riusato dal generatore pubblico.
- `namegenerator.result.NameResult` — `record NameResult(String name) implements GeneratedElementResult`, con `Builder` interno fluente (`NameResult.builder().name(...).build()`).
- `namegenerator.CharacterNameGeneratorTool` — generatore pubblico dei nomi. `building()` → `Builder`; `race(Race)` e `addNickname()` sono metodi fluenti invocabili in qualsiasi ordine; `generate()` produce il `NameResult`. Il nome è quello di razza (lista scelta dalla `Race`); con `addNickname()` diventa `name + " " + nickname`, con nickname da `nicknames.txt`. `generate()` senza `race()` → `IllegalStateException`.

```java
CharacterNameGeneratorTool.building().addNickname().race(Race.HUMAN).generate();   // nome + nickname
CharacterNameGeneratorTool.building().race(Race.ORC).generate();                   // solo nome di razza
```

### `jewelgenerator` — generazione di gioielli con rarità

- `jewelgenerator.JewelGeneratorTool` — `building()` → `Builder` → `generate()` → `JewelResult`.
  - **Tipo di gioiello**: `jewel(Jewel)` per uno fisso, oppure `randomJewel()` per uno casuale tra tutti i `Jewel.values()`. Nessuno dei due → `IllegalStateException`.
  - **Rarità**: esattamente **una** tra `rarity(Rarity)` (rarità fissa), `maxRarity(Rarity)` (casuale fino a quel livello incluso, per `ordinal()`), `rarityTable(RarityTable)` (estrazione pesata) e `randomRarity()` (casuale tra tutte le `Rarity.values()`). Zero o più di una fonte → `IllegalStateException`.
  - **Buff/debuff**: di default il gioiello riceve i propri status effect delegando internamente a `BuffDebuffGeneratorTool` con la rarità risolta (i debuff restano oggi sempre vuoti, vedi `buffdebuffgenerator`). `noStatusEffect()` produce invece un gioiello **senza** status effect (`buffs` e `debuffs` vuoti) saltando quella chiamata; è combinabile con qualsiasi sorgente di gioiello/rarità e non incide su tipo e rarità.
- `jewelgenerator.result.JewelResult` — `record JewelResult(Jewel jewel, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs) implements GeneratedElementResult`, con `Builder` interno. `buffs`/`debuffs` riusano i record di `buffdebuffgenerator.result`.

```java
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.EPIC).generate();
JewelGeneratorTool.building().randomJewel().maxRarity(Rarity.RARE).generate();
JewelGeneratorTool.building().jewel(Jewel.NECKLACE).rarityTable(table).generate();
JewelGeneratorTool.building().randomJewel().randomRarity().generate();
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.COMMON).noStatusEffect().generate();  // buffs/debuffs vuoti
```

### `buffdebuffgenerator` — generazione di buff/debuff per rarità

- `buffdebuffgenerator.BuffDebuffGeneratorTool` — `building().rarity(Rarity)...generate()` → `BuffDebuffResult`. `rarity(...)` è obbligatoria (altrimenti `IllegalStateException`); `rules(BuffDebuffRules)` è opzionale e di default usa `DefaultBuffDebuffRules`.
  - La rarità seleziona la lista di `BuffCombination` ammesse; se ne pesca una a caso. Ogni combinazione definisce `count` (quanti buff) e l'intervallo `[minValue, maxValue]` dei valori. Le caratteristiche vengono mescolate e se ne prendono le prime `count`, ciascuna con un valore casuale nell'intervallo.
  - **Stato attuale**: i **debuff non sono ancora generati** — `debuffs` è sempre `List.of()`. La struttura per estenderli esiste (`DebuffElement`, `StatusEffect`), ma la logica di produzione va ancora scritta.
- `buffdebuffgenerator.result` — `BuffDebuffResult(List<BuffElement> buffs, List<DebuffElement> debuffs)`; `BuffElement`/`DebuffElement` sono record `(Characteristic, int value)` che implementano l'interfaccia comune `StatusEffect`.
- `buffdebuffgenerator.rules` — `BuffDebuffRules` (interfaccia: `combinationsFor(Rarity)`), `DefaultBuffDebuffRules` (mappa `Rarity` → combinazioni, `EnumMap`), `BuffCombination(int count, int minValue, int maxValue)`. Le regole sono un **punto di estensione**: passando una `BuffDebuffRules` custom si cambiano combinazioni e range senza toccare il tool.

### `weapongenerator` — generazione di armi con rarità e status effect

- `weapongenerator.WeaponGeneratorTool` — `building()` → `Builder` → `generate()` → `WeaponResult`. **Copia 1:1 del pattern di `JewelGeneratorTool`**, cambia solo il tipo generato.
  - **Tipo di arma**: `weapon(Weapon)` per una fissa, oppure `randomWeapon()` per una casuale tra tutti i `Weapon.values()`. Nessuno dei due o entrambi → `IllegalStateException`.
  - **Rarità**: esattamente **una** tra `rarity(Rarity)`, `maxRarity(Rarity)`, `rarityTable(RarityTable)` e `randomRarity()`. Zero o più di una fonte → `IllegalStateException`.
  - **Buff/debuff**: come Jewel, di default delega a `BuffDebuffGeneratorTool` con la rarità risolta; `noStatusEffect()` produce un'arma con `buffs`/`debuffs` vuoti, senza toccare tipo e rarità.
  - **Attacco**: ogni arma ha un `attack` (int) pescato casualmente in un intervallo che dipende dalla rarità. L'intervallo è fornito da `WeaponRules` (default `DefaultWeaponRules`); `rules(WeaponRules)` è opzionale e permette range custom.
- `weapongenerator.rules` — `AttackRange(int minValue, int maxValue)` (gemello di `BuffCombination`), interfaccia `WeaponRules` (`attackFor(Rarity)`), `DefaultWeaponRules` (mappa `Rarity` → `AttackRange`, `EnumMap`). Range di default crescenti: COMMON `[1,3]`, UNCOMMON `[3,6]`, RARE `[6,10]`, EPIC `[10,15]`, LEGENDARY `[15,25]`.
- `weapongenerator.result.WeaponResult` — `record WeaponResult(Weapon weapon, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs, int attack) implements GeneratedElementResult`, con `Builder` interno.

```java
WeaponGeneratorTool.building().weapon(Weapon.SWORD).rarity(Rarity.EPIC).generate();
WeaponGeneratorTool.building().randomWeapon().maxRarity(Rarity.RARE).generate();
WeaponGeneratorTool.building().weapon(Weapon.BOW).rarity(Rarity.COMMON).noStatusEffect().generate();  // buffs/debuffs vuoti
```

### `armourgenerator` — generazione di armature con rarità e status effect

- `armourgenerator.ArmourGeneratorTool` — `building()` → `Builder` → `generate()` → `ArmourResult`. Identico a `WeaponGeneratorTool`, con `armour(Armour)` / `randomArmour()` al posto dell'arma. Stesse quattro fonti di rarità e stesso `noStatusEffect()`. Ogni armatura ha un `defense` (int) pescato in un intervallo dipendente dalla rarità, fornito da `ArmourRules` (default `DefaultArmourRules`); `rules(ArmourRules)` opzionale.
- `armourgenerator.rules` — `DefenseRange(int minValue, int maxValue)`, interfaccia `ArmourRules` (`defenseFor(Rarity)`), `DefaultArmourRules` (`EnumMap`). Range di default: COMMON `[1,2]`, UNCOMMON `[2,4]`, RARE `[4,7]`, EPIC `[7,11]`, LEGENDARY `[11,18]`.
- `armourgenerator.result.ArmourResult` — `record ArmourResult(Armour armour, Rarity rarity, List<BuffElement> buffs, List<DebuffElement> debuffs, int defense) implements GeneratedElementResult`, con `Builder` interno.

```java
ArmourGeneratorTool.building().armour(Armour.HELMET).rarity(Rarity.LEGENDARY).generate();
ArmourGeneratorTool.building().randomArmour().rarityTable(table).generate();
ArmourGeneratorTool.building().armour(Armour.BOOTS).randomRarity().noStatusEffect().generate();  // buffs/debuffs vuoti
```

> **Nota sulla duplicazione.** `JewelGeneratorTool`, `WeaponGeneratorTool` e `ArmourGeneratorTool` condividono per copia la logica di risoluzione rarità + buff/debuff. Scelta coerente con lo stile self-contained del progetto; un'eventuale centralizzazione in `core` sarebbe un refactor trasversale a tutti e tre.

Non ci sono database né configurazione esterna: le liste `.txt` sono l'unica origine dati per i nomi. Aggiungere un nome = aggiungere una riga al file corrispondente.

## Convenzioni del progetto

- **Codice in inglese** (classi, metodi, variabili, messaggi). Anche i **nomi dei file di risorse sono in inglese** e vanno mantenuti stabili così come sono: `humans_names.txt`, `elves_names.txt`, `orks_names.txt`, `undeads_names.txt`, `nicknames.txt`.
- **Risorse**: tutte le liste stanno in `src/main/resources/namegenerator/` e si caricano dalla **radice del classpath** con path assoluto `/namegenerator/<file>.txt`. Copia unica in `main`: i test le leggono dal classpath di test (che include `main/resources`), quindi **non** duplicare i file sotto `src/test/resources`.
- **Package**: i tool stanno sotto `it.fantasytoolkit.*` (coerente con `groupId`), mentre il modello di dominio sta sotto `it.fantasytoolkitcore.core.*`. I `*Tool` hanno costruttore privato ed entry-point statico `building()`.
- I test verificano che il nome generato appartenga effettivamente al dizionario sorgente, rileggendo il file di parole in modo indipendente con l'helper di test `tools.FileReader.readLines(path)` (restituisce un `Set<String>`).
