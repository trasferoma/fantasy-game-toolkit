# Documentazione delle API pubbliche — fantasy-game-toolkit

Questo indice raccoglie la documentazione destinata ai **consumatori** della
libreria. Ogni documento descrive un modulo logico (package) come contratto
pubblico: API, builder, parametri, risultato, errori ed esempi.

## Come consultare questa documentazione

1. parti sempre da questo `INDEX.md`;
2. leggi soltanto il documento del modulo che ti serve;
3. considera la documentazione come **contratto pubblico**;
4. ispeziona l'implementazione soltanto quando devi **modificare** la libreria
   oppure quando la documentazione risulta incompleta o incoerente col codice.

## Convenzioni comuni a tutti i tool

- Ogni generatore pubblico è un `*Tool` con **costruttore privato** ed
  entry-point statico `building()` che restituisce un `Builder` interno fluente.
- La catena del builder si chiude con `generate()` (o `roll()` per
  `DiceLauncherTool`) e restituisce un **result immutabile** (`record`).
- Ogni result implementa `GeneratedElementResult` (marker interface vuota,
  vedi [core](core.md)).
- La generazione usa internamente `new Random()`: è **casuale e non
  riproducibile** (nessun seme esposto).
- I metodi del builder sono invocabili in **qualsiasi ordine**; la validazione
  avviene in `generate()`/`roll()` (salvo `DiceLauncherTool.dice(...)`, che valida
  subito). Gli errori di configurazione sono `IllegalStateException`.

## Moduli

| Documento | Modulo | Scopo |
|-----------|--------|-------|
| [core.md](core.md) | `core` | Enum di dominio, tabelle rarità/bonus, marker result |
| [name-generator.md](name-generator.md) | `namegenerator` | Nomi di personaggio da liste del classpath |
| [jewel-generator.md](jewel-generator.md) | `jewelgenerator` | Gioielli con rarità e status effect |
| [buff-debuff-generator.md](buff-debuff-generator.md) | `buffdebuffgenerator` | Buff/debuff per rarità (usato dagli altri tool) |
| [weapon-generator.md](weapon-generator.md) | `weapongenerator` | Armi con rarità, status effect e attacco |
| [armour-generator.md](armour-generator.md) | `armourgenerator` | Armature con rarità, status effect e difesa |
| [potion-generator.md](potion-generator.md) | `potiongenerator` | Pozioni per famiglia e rarità |
| [character-generator.md](character-generator.md) | `charactergenerator` | Personaggi con razza, classe, nome, caratteristiche |
| [dungeon-generator.md](dungeon-generator.md) | `dungeongenerator` | Dungeon a stanze collegate (+ renderer ASCII) |
| [dice-launcher.md](dice-launcher.md) | `dicelauncher` | Lancio di gruppi di dadi |
