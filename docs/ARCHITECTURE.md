# Architecture

## Design goal

The two requested versions must behave identically. To guarantee that, the code
uses one shared engine and treats the command line and Swing window as adapters.
Neither interface is permitted to move cards directly.

```text
Terminal input ──> CLI parser ──┐
                               ├──> KlondikeGame ──> MoveResult + read-only state
Mouse clicks ────> Swing UI ───┘
```

## Engine package

`KlondikeGame` owns every mutable pile and is the single authority for game
rules. The end of each internal list represents the pile's top. Public accessors
return immutable copies, preventing a renderer from changing state accidentally.

`Card` owns immutable rank and suit values plus the orientation that changes as
cards are dealt, revealed, drawn, and restocked. `Rank` and `Suit` replace magic
integers and characters with explicit domain values.

Every attempted action returns `MoveResult`. A failed result guarantees that no
cards moved, while its message gives either interface useful player feedback.
The engine never prints text or imports Swing.

## Command-line package

`CommandLineSolitaire` handles input, aliases, numeric conversion, and lifecycle.
`ConsoleRenderer` renders a snapshot of the engine. Human-facing tableau numbers
are one through seven; the parser converts them to zero-based engine indexes.

The optional seed argument creates repeatable deals, which is useful when filing
a bug: another developer can start the exact same board.

## Swing package

`SolitaireFrame` converts clicks into engine actions and rebuilds the visible
board from authoritative state after each action. The temporary selection object
belongs only to the UI and never changes the rules.

Cards and empty slots are custom lightweight Swing components painted with Java
2D. They require no bundled bitmaps, remain crisp at normal desktop resolutions,
and visibly distinguish red suits, card backs, empty destinations, and the
currently selected source.

Swing creation runs on the Event Dispatch Thread. Game actions are currently
small and synchronous; if later features add expensive solving or hints, that
work should run in a background worker and publish results back to the EDT.

## Testing strategy

Engine tests cover deal invariants, repeatable shuffles, stock order through a
restock, legal and illegal tableau runs, King-only empty columns, foundations,
rollback, and immutable state views. A package-private controlled-state
constructor makes each rule test small and readable without exposing a setup
backdoor to production interfaces.

Interface code intentionally stays thin. Parser tests and Swing UI automation can
be added later, but the high-risk state transitions already live in the tested
engine rather than in either presentation layer.
