# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandi

Progetto Maven, **Java 21**. La macchina ha come default un JDK 17 (`JAVA_HOME` → `C:\lavoro\jdk\openlogic-openjdk-17...`), che **non compila** (`invalid target release: 21`). Per i comandi Maven punta `JAVA_HOME` al JDK 21:

```bash
export JAVA_HOME="/c/Users/fabio.dearcangelis/.jdks/ms-21.0.9"   # git bash

mvn test                                   # compila ed esegue tutti i test
mvn test -Dtest=HeroNameGeneratorToolTest  # una singola classe di test
mvn test -Dtest=EnemyNameGeneratorToolTest#generatesElfNameFromDictionary  # un singolo metodo
mvn compile                                # sola compilazione
mvn package                                # build completa
```

Test: JUnit Jupiter (JUnit 5) + AssertJ (`assertThat(...)`). Surefire con `useModulePath=false`. Lombok è `provided` (`@Getter` risolto a compile-time).

## Architettura

Toolkit per giochi fantasy, organizzato in **moduli logici (package)** dentro un singolo modulo Maven:

- **`core`** — modello di dominio. `core.model.Race` è un enum (`HUMAN`/`ELF`/`ORC`/`UNDEAD`) che mappa ogni razza al proprio file di nomi (`namesFile`) e a un `symbol` char. È il punto di estensione per nuove razze: nuova costante + relativo file di risorse.
- **`namegenerator`** — generazione di nomi a partire da liste di parole caricate dal classpath.
  - `NameGeneratorTool` (costruttore package-private): motore base. Carica un file dal classpath (una parola per riga, UTF-8, righe vuote scartate) e restituisce una voce casuale con `generateName()`. Se la risorsa non esiste o la lista è vuota lancia `IllegalStateException` con messaggio diagnostico. È il mattone riusato dai generatori pubblici.
  - `HeroNameGeneratorTool.generateName()`: compone `name + " " + epithet` usando **due** liste distinte (`nomi_eroi.txt`, `aggettivi_eroi.txt`).
  - `EnemyNameGeneratorTool.generateName(Race)`: nome singolo, lista scelta in base alla `Race`.

Non ci sono database né configurazione esterna: le liste `.txt` sono l'unica origine dati. Aggiungere un nome = aggiungere una riga al file corrispondente.

## Convenzioni del progetto

- **Codice in inglese** (classi, metodi, variabili, messaggi). I **nomi dei file di risorse restano in italiano** per scelta (`nomi_eroi.txt`, `aggettivi_eroi.txt`, `nomi_umani.txt`, `nomi_elfi.txt`, `nomi_orchi.txt`, `nomi_undead.txt`): mantieni questa convenzione.
- **Risorse**: tutte le liste stanno in `src/main/resources/namegenerator/` e si caricano dalla **radice del classpath** con path assoluto `/namegenerator/<file>.txt`. Copia unica in `main`: i test le leggono dal classpath di test (che include `main/resources`), quindi **non** duplicare i file sotto `src/test/resources`.
- **Package** `it.fantasytoolkit` (coerente con `groupId`). I `*Tool` con soli metodi statici hanno costruttore privato per impedirne l'istanziazione.
- I test verificano che il nome generato appartenga effettivamente al dizionario sorgente, rileggendo il file di parole in modo indipendente (`AbstractNameGeneratorTest.readLines`).
