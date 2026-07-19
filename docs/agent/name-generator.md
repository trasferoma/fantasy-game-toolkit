# name-generator — generazione di nomi da liste del classpath

Package: `it.fantasytoolkit.namegenerator`.

Genera nomi di personaggio pescando parole da file di testo del classpath (una
parola per riga, UTF-8, righe vuote scartate). L'API pubblica è
`CharacterNameGeneratorTool`; `NameGeneratorTool` è il motore base riusabile.

## `CharacterNameGeneratorTool`

Entry-point: `CharacterNameGeneratorTool.building()` → `Builder` → `generate()`
→ `NameResult`.

### Builder

| Metodo | Obbligo | Effetto |
|--------|---------|---------|
| `race(Race race)` | **obbligatorio** | sceglie la lista di nomi in base alla razza |
| `addNickname()` | opzionale | aggiunge un soprannome da `nicknames.txt` |
| `generate()` | chiusura | produce il `NameResult` |

`race(...)` e `addNickname()` sono invocabili in qualsiasi ordine.

### Comportamento

- Senza `addNickname()`: il nome è quello di razza (lista scelta dalla `Race`).
- Con `addNickname()`: il nome diventa `nome + " " + nickname`, con nickname
  pescato da `nicknames.txt`.

### Risultato — `NameResult`

`record NameResult(String name) implements GeneratedElementResult`.

Builder proprio: `NameResult.builder().name(...).build()`.

### Errori e vincoli

- `generate()` senza `race(...)` → `IllegalStateException`
  ("Race must be set before generating a name").
- File di nomi mancante nel classpath → `IllegalStateException`
  ("Names file not found: ...").
- Lista vuota (nessuna parola valida) → `IllegalStateException`
  ("Name list not loaded correctly").

### Esempi

```java
// nome di razza + soprannome
NameResult withNick = CharacterNameGeneratorTool.building()
        .addNickname().race(Race.HUMAN).generate();

// solo nome di razza
NameResult onlyName = CharacterNameGeneratorTool.building()
        .race(Race.ORC).generate();

String name = onlyName.name();
```

## `namegenerator.tool.NameGeneratorTool` (motore base)

Mattone riusato dal generatore pubblico. Costruttore **pubblico** che riceve il
path del file di parole nel classpath.

- `new NameGeneratorTool(String namesFile)` — carica il file (UTF-8, righe
  vuote scartate). Risorsa mancante → `IllegalStateException`.
- `pick(Random random)` — **unico punto di selezione**; estrae una parola con un
  `Random` fornito dall'esterno. Lista vuota → `IllegalStateException`.

## Origine dati

Le liste `.txt` sono l'unica origine dati. Stanno in
`src/main/resources/namegenerator/` e si caricano dalla radice del classpath con
path assoluto `/namegenerator/<file>.txt`. File esistenti (nomi in inglese, da
mantenere stabili): `humans_names.txt`, `elves_names.txt`, `orks_names.txt`,
`undeads_names.txt`, `nicknames.txt`. Aggiungere un nome = aggiungere una riga.
