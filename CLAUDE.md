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

Toolkit per giochi fantasy, organizzato in **moduli logici (package)** dentro un singolo modulo Maven:

- **`core`** — modello di dominio e tipi condivisi.
  - `core.model.Race` è un enum (`HUMAN`/`ELF`/`ORC`/`UNDEAD`) che mappa ogni razza al proprio file di nomi (`namesFile`) e a un `symbol` char. È il punto di estensione per nuove razze: nuova costante + relativo file di risorse.
  - `core.types.Seed` è un `record Seed(long value)`: dato puro, nessuna conversione. `toString()` restituisce il valore come numero decimale (`Long.toString(value)`), così un seed è leggibile e copiabile.
  - `core.types.SeedBuilder` genera **solo** seed casuali (`SeedBuilder.newSeed().build()`), nascondendo `ThreadLocalRandom`. Un seed noto (per riprodurre) si costruisce con `new Seed(long)`, non dal builder.
  - `core.pojo.GeneratedElementResult` è un'**interfaccia** con il solo contratto `Seed seed()`: contratto comune dei risultati, condiviso dai tool futuri senza vincolare l'ereditarietà.
- **`namegenerator`** — generazione di nomi a partire da liste di parole caricate dal classpath.
  - `NameGeneratorTool` (costruttore pubblico): motore base. Carica un file dal classpath (una parola per riga, UTF-8, righe vuote scartate). L'estrazione avviene con `pick(Random)`, **unico punto di selezione**, con un `Random` fornito dall'esterno (quindi seminabile). Se la risorsa non esiste (costruttore) o la lista è vuota (`pick`) lancia `IllegalStateException` con messaggio diagnostico. È il mattone riusato dai generatori pubblici.
  - `namegenerator.result.NameResult` è un `record NameResult(String name, Seed seed) implements GeneratedElementResult`, immutabile, con `Builder` interno fluente (`NameResult.builder().name(...).seed(...).build()`). È il tipo di ritorno del generatore pubblico.
  - `CharacterNameGeneratorTool`: **unico** generatore pubblico, con fluent interface. Entry-point statici `race(Race)` e `addNickname()` aprono un `Builder` interno; `race(Race)`/`addNickname()`/`useSeed(Seed)` esistono come metodi d'istanza (ritornano `this`); `generate()` chiude la catena e produce il `NameResult`, **sempre senza parametri**: usa il seed impostato con `useSeed(Seed)` se presente, altrimenti ne crea uno casuale con `SeedBuilder`. Il nome è quello di razza (lista scelta dalla `Race`); con `addNickname()` diventa la stringa composta `name + " " + nickname`, dove il nickname arriva da `nicknames.txt`. `generate()` senza `race()` lancia `IllegalStateException`.

```java
CharacterNameGeneratorTool.addNickname().race(Race.HUMAN).generate();              // nome + nickname, seed casuale
CharacterNameGeneratorTool.race(Race.ORC).generate();                              // solo nome di razza, seed casuale
CharacterNameGeneratorTool.race(Race.ORC).useSeed(seed).generate();                // riproducibile
```

Non ci sono database né configurazione esterna: le liste `.txt` sono l'unica origine dati. Aggiungere un nome = aggiungere una riga al file corrispondente.

### Seed e riproducibilità (scelta architetturale centrale)

I generatori ritornano un `NameResult` (nome + `Seed`), **non** una `String`. Il `Seed` è il **seme del PRNG**: dal suo `long` si inizializza `new Random(seed.value())`; non incorpora il valore selezionato.

- **Due percorsi**: uno "casuale" (`generate()` senza `useSeed(...)` impostato crea un nuovo `Seed` con `SeedBuilder` e genera) e uno "da seed" (`useSeed(Seed).generate()` riproduce a partire da un `Seed` dato). Entrambi convergono nello stesso `generate()` → unico cammino di estrazione, zero duplicazione.
- **Riproducibilità**: passando con `useSeed(...)` lo stesso `Seed` restituito da una generazione precedente si riottiene lo stesso `name`, **a parità di dizionari** (contenuto e ordine delle liste `.txt` invariati).
- **Ordine di pesca fisso**: quando da un solo seed servono più estrazioni (con `addNickname()`: prima il nome di razza, poi il nickname) si usa **lo stesso** `Random` seminato e l'ordine è fisso e documentato. Senza nickname si esegue una sola estrazione.

```java
NameResult first = CharacterNameGeneratorTool.race(Race.ORC).generate();
Seed seed = first.seed();
NameResult again = CharacterNameGeneratorTool.race(Race.ORC).useSeed(seed).generate();
assert again.name().equals(first.name());   // a dizionari invariati
```

## Convenzioni del progetto

- **Codice in inglese** (classi, metodi, variabili, messaggi). Anche i **nomi dei file di risorse sono in inglese** e vanno mantenuti stabili così come sono: `humans_names.txt`, `elves_names.txt`, `orks_names.txt`, `undeads_names.txt`, `nicknames.txt`.
- **Risorse**: tutte le liste stanno in `src/main/resources/namegenerator/` e si caricano dalla **radice del classpath** con path assoluto `/namegenerator/<file>.txt`. Copia unica in `main`: i test le leggono dal classpath di test (che include `main/resources`), quindi **non** duplicare i file sotto `src/test/resources`.
- **Package** `it.fantasytoolkit` (coerente con `groupId`). I `*Tool` con soli metodi statici hanno costruttore privato per impedirne l'istanziazione.
- I test verificano che il nome generato appartenga effettivamente al dizionario sorgente, rileggendo il file di parole in modo indipendente (`AbstractNameGeneratorTest.readLines`).
