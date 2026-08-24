# Contributing

## Local setup

Use JDK 17 or newer and Maven 3.9 or newer. Run `mvn clean test` before proposing
a change. Start either interface using the commands in the README.

## Change workflow

1. Create a short-lived branch from the current default branch.
2. Put rule changes in `engine` first and add a focused test.
3. Expose the behavior in both interfaces when it is player-facing.
4. Update rules or architecture documentation when behavior changes.
5. Use a focused commit subject such as `feat(engine): add undo history`.

Commit bodies should explain the motivation, important design choices, and any
player-visible behavior. Avoid mixing formatting-only changes with new rules.

## Code style

- Use four spaces and descriptive domain names.
- Document public classes and methods with Javadoc where their contract is not
  obvious from the signature.
- Add comments for design intent or non-obvious ordering, not for every line.
- Keep rendering and input concerns outside the engine.
- Return useful validation results instead of printing inside domain classes.
- Preserve immutable public views of piles.

## Testing

Every rule change should include a legal case, an illegal case, and an assertion
that a rejected action leaves state unchanged. Prefer deterministic seeds when a
test needs a normal deal and a controlled state when testing one exact rule.
