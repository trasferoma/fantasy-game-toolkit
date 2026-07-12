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
  - **Rarità**: esattamente **una** tra `rarity(Rarity)` (rarità fissa), `maxRarity(Rarity)` (casuale fino a quel livello incluso, per `ordinal()`) e `rarityTable(RarityTable)` (estrazione pesata). Zero o più di una fonte → `IllegalStateException`.
- `jewelgenerator.result.JewelResult` — `record JewelResult(Jewel jewel, Rarity rarity) implements GeneratedElementResult`, con `Builder` interno.

```java
JewelGeneratorTool.building().jewel(Jewel.RING).rarity(Rarity.EPIC).generate();
JewelGeneratorTool.building().randomJewel().maxRarity(Rarity.RARE).generate();
JewelGeneratorTool.building().jewel(Jewel.NECKLACE).rarityTable(table).generate();
```

### `buffdebuffgenerator` — generazione di buff/debuff per rarità

- `buffdebuffgenerator.BuffDebuffGeneratorTool` — `building().rarity(Rarity)...generate()` → `BuffDebuffResult`. `rarity(...)` è obbligatoria (altrimenti `IllegalStateException`); `rules(BuffDebuffRules)` è opzionale e di default usa `DefaultBuffDebuffRules`.
  - La rarità seleziona la lista di `BuffCombination` ammesse; se ne pesca una a caso. Ogni combinazione definisce `count` (quanti buff) e l'intervallo `[minValue, maxValue]` dei valori. Le caratteristiche vengono mescolate e se ne prendono le prime `count`, ciascuna con un valore casuale nell'intervallo.
  - **Stato attuale**: i **debuff non sono ancora generati** — `debuffs` è sempre `List.of()`. La struttura per estenderli esiste (`DebuffElement`, `StatusEffect`), ma la logica di produzione va ancora scritta.
- `buffdebuffgenerator.result` — `BuffDebuffResult(List<BuffElement> buffs, List<DebuffElement> debuffs)`; `BuffElement`/`DebuffElement` sono record `(Characteristic, int value)` che implementano l'interfaccia comune `StatusEffect`.
- `buffdebuffgenerator.rules` — `BuffDebuffRules` (interfaccia: `combinationsFor(Rarity)`), `DefaultBuffDebuffRules` (mappa `Rarity` → combinazioni, `EnumMap`), `BuffCombination(int count, int minValue, int maxValue)`. Le regole sono un **punto di estensione**: passando una `BuffDebuffRules` custom si cambiano combinazioni e range senza toccare il tool.

Non ci sono database né configurazione esterna: le liste `.txt` sono l'unica origine dati per i nomi. Aggiungere un nome = aggiungere una riga al file corrispondente.

## Convenzioni del progetto

- **Codice in inglese** (classi, metodi, variabili, messaggi). Anche i **nomi dei file di risorse sono in inglese** e vanno mantenuti stabili così come sono: `humans_names.txt`, `elves_names.txt`, `orks_names.txt`, `undeads_names.txt`, `nicknames.txt`.
- **Risorse**: tutte le liste stanno in `src/main/resources/namegenerator/` e si caricano dalla **radice del classpath** con path assoluto `/namegenerator/<file>.txt`. Copia unica in `main`: i test le leggono dal classpath di test (che include `main/resources`), quindi **non** duplicare i file sotto `src/test/resources`.
- **Package**: i tool stanno sotto `it.fantasytoolkit.*` (coerente con `groupId`), mentre il modello di dominio sta sotto `it.fantasytoolkitcore.core.*`. I `*Tool` hanno costruttore privato ed entry-point statico `building()`.
- I test verificano che il nome generato appartenga effettivamente al dizionario sorgente, rileggendo il file di parole in modo indipendente con l'helper di test `tools.FileReader.readLines(path)` (restituisce un `Set<String>`).
