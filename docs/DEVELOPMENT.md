# Development Stages and Commit Plan

Development is recorded as focused commits so GitHub history explains not just
what changed, but why each stage exists. The initial repository contained card,
deck, waste, and tableau experiments in the default package.

## Stage 1 — Shared rules engine

**Commit:** `feat(engine): implement complete Klondike rules`

Replace the prototypes with typed domain objects and one interface-independent
game. Complete dealing, drawing, unlimited restocking, tableau movement,
foundations, card reveals, validation feedback, and win detection. Establish a
Java 17 Maven build so later stages have a consistent project structure.

## Stage 2 — Command-line version

**Commit:** `feat(cli): add interactive command-line game`

Add a readable text board, full and abbreviated commands, input validation,
reproducible seeds, and all move directions supported by the engine.

## Stage 3 — Swing version

**Commit:** `feat(swing): add graphical Solitaire interface`

Add a native graphical window with vector-painted cards, click-based selection,
scrollable tableau, feedback messages, instructions, victory dialog, and new-game
controls. Keep it dependency-free by using only Swing and Java 2D.

## Stage 4 — Tests and documentation

**Commit:** `test(docs): verify rules and document both editions`

Add focused rule tests, public setup and usage instructions, the supported-rules
contract, architecture notes, contribution guidance, and ignored Maven output.

## Stage 5 — Continuous integration

**Commit:** `ci: verify Java 17 build on GitHub Actions`

Run the full Maven test suite on every push and pull request so GitHub displays a
clear build result for each future development step.

## Suggested future stages

Future work should remain similarly focused. Good candidates are undo/redo with
a command object history, optional hints using a move enumerator, persistent
statistics, accessibility improvements, and selectable draw-three rules. Each
feature should begin in the engine, gain tests, and then be surfaced in both UIs.
