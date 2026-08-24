# Solitaire from Scratch in Java

A complete, from-scratch implementation of draw-one Klondike Solitaire with two
interfaces over one rules engine:

- a command-line version suitable for terminals and learning the rules; and
- a graphical Java Swing version with vector-drawn cards and click-to-move play.

The game uses unlimited stock restocks. It requires Java 17 or newer and has no
runtime dependencies beyond the Java standard library.

## Quick start

Install a Java 17+ JDK and [Apache Maven](https://maven.apache.org/), then build:

```text
mvn clean test
```

Run the command-line edition:

```text
mvn -q exec:java -Dexec.mainClass=com.benjaminle.solitaire.cli.CommandLineSolitaire
```

Run the Swing edition:

```text
mvn -q exec:java -Dexec.mainClass=com.benjaminle.solitaire.swing.SwingSolitaire
```

If you prefer not to use Maven to launch, compile once with `mvn package`, then
run either main class from `target/classes`:

```text
java -cp target/classes com.benjaminle.solitaire.cli.CommandLineSolitaire
java -cp target/classes com.benjaminle.solitaire.swing.SwingSolitaire
```

## Graphical controls

1. Click the stock to draw one card. When it is empty, click it to restock.
2. Click a face-up tableau, waste, or foundation card to select it.
3. Click a tableau or foundation destination to attempt the move.
4. Selecting a tableau card selects the entire ordered run beneath it.

Illegal moves leave the board unchanged and display an explanation along the
bottom of the window. Cards are drawn with Java 2D, so no image download or
asset setup is required.

## Command-line controls

Enter `help` during a terminal game for the complete command list. Common moves:

```text
draw
move waste tableau 4
move waste foundation
move tableau 3 foundation
move tableau 5 tableau 2 3
move foundation hearts table 6
```

Short forms are supported, so the first example can be written `m w t 4`.
Pass `--seed NUMBER` to reproduce a particular shuffled deal.

## Project structure

```text
src/main/java/com/benjaminle/solitaire/
├── engine/   shared cards, state, dealing, and move validation
├── cli/      terminal parser and text board renderer
└── swing/    graphical window and custom card components

src/test/java/com/benjaminle/solitaire/engine/
└── KlondikeGameTest.java
```

Both interfaces call the same `KlondikeGame` object. This separation is the
central design decision: fixing a rule once fixes it for both versions.

## Documentation

- [Rules and supported variation](docs/RULES.md)
- [Architecture and data flow](docs/ARCHITECTURE.md)
- [Development stages and commit plan](docs/DEVELOPMENT.md)
- [Contributing guide](CONTRIBUTING.md)

## Current capabilities

- standard seven-column Klondike deal;
- draw-one stock and unlimited, correctly ordered restocks;
- alternating-color, descending tableau construction;
- movement of complete face-up tableau runs;
- automatic reveal of newly exposed tableau cards;
- same-suit foundations from Ace through King;
- legal foundation-to-tableau rollback;
- win detection after all 52 cards reach foundations;
- deterministic seeded games for reproduction and testing; and
- immutable state views that prevent interfaces from bypassing the rules.

## License

No license file was present in the original repository. The repository owner
should choose and add a license before inviting outside redistribution or reuse.
