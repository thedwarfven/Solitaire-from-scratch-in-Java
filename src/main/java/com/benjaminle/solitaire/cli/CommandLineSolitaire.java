package com.benjaminle.solitaire.cli;

import com.benjaminle.solitaire.engine.KlondikeGame;
import com.benjaminle.solitaire.engine.MoveResult;
import com.benjaminle.solitaire.engine.Suit;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Interactive command-line version of Solitaire.
 *
 * <p>This class translates text commands into calls to {@link KlondikeGame};
 * no game rule is duplicated here. Run without arguments for a random deal or
 * pass {@code --seed NUMBER} to reproduce a deal.</p>
 */
public final class CommandLineSolitaire {
    private static final String HELP = """
            Commands:
              draw                         draw one card, or restock an empty stock
              move waste tableau N         move waste card to tableau column N
              move waste foundation        move waste card to its foundation
              move tableau N foundation    move tableau N's top card to foundation
              move tableau A tableau B C   move C cards from tableau A to B
              move foundation SUIT table N move a foundation card back to tableau N
              show                         redraw the board
              new                          shuffle and begin a new game
              help                         show this command list
              quit                         exit

            Short forms: d, m, w, t, f, s, n, h, q. Suits accept c/d/h/s.
            Examples: "m w t 4", "m t 3 f", "m t 5 t 2 3", "m f h t 6"
            """;

    private final KlondikeGame game;
    private final ConsoleRenderer renderer = new ConsoleRenderer();

    public CommandLineSolitaire(KlondikeGame game) {
        this.game = game;
    }

    public static void main(String[] args) {
        KlondikeGame game = parseSeed(args);
        new CommandLineSolitaire(game).run();
    }

    /** Runs until the player enters {@code quit} or closes standard input. */
    public void run() {
        System.out.println("Klondike Solitaire — draw one, unlimited restocks");
        System.out.println("Enter 'help' for commands.");
        System.out.print(renderer.render(game));

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(System.lineSeparator() + "> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (is(line, "quit", "q", "exit")) {
                    break;
                }
                process(line);
            }
        }
        System.out.println("Thanks for playing.");
    }

    private void process(String line) {
        String[] tokens = line.toLowerCase(Locale.ROOT).split("\\s+");
        try {
            if (is(tokens[0], "help", "h", "?")) {
                System.out.println(HELP);
                return;
            }
            if (is(tokens[0], "show", "s")) {
                System.out.print(renderer.render(game));
                return;
            }
            if (is(tokens[0], "new", "n")) {
                game.newGame();
                report(MoveResult.success("Started a new shuffled game."));
                return;
            }
            if (is(tokens[0], "draw", "d")) {
                report(game.draw());
                return;
            }
            if (is(tokens[0], "move", "m")) {
                report(parseMove(List.of(tokens).subList(1, tokens.length)));
                return;
            }
            System.out.println("Unknown command. Enter 'help' to see valid commands.");
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            System.out.println("Invalid command: " + exception.getMessage());
        }
    }

    private MoveResult parseMove(List<String> words) {
        if (words.size() < 2) {
            throw new IllegalArgumentException("a move needs a source and destination");
        }
        String source = words.get(0);
        if (is(source, "waste", "w")) {
            if (is(words.get(1), "foundation", "f")) {
                return game.moveWasteToFoundation();
            }
            int target = parseTableau(words, 1);
            return game.moveWasteToTableau(target);
        }
        if (is(source, "tableau", "table", "t")) {
            requireSize(words, 3);
            int sourceColumn = parseColumn(words.get(1));
            if (is(words.get(2), "foundation", "f")) {
                return game.moveTableauToFoundation(sourceColumn);
            }
            int targetColumn = parseTableau(words, 2);
            int count = words.size() >= 5 ? positiveNumber(words.get(4), "card count") : 1;
            List<?> sourceCards = game.tableau(sourceColumn);
            return game.moveTableauToTableau(sourceColumn, sourceCards.size() - count, targetColumn);
        }
        if (is(source, "foundation", "f")) {
            requireSize(words, 4);
            Suit suit = Suit.parse(words.get(1));
            int targetColumn = parseTableau(words, 2);
            return game.moveFoundationToTableau(suit, targetColumn);
        }
        throw new IllegalArgumentException("source must be waste, tableau, or foundation");
    }

    private static int parseTableau(List<String> words, int nameIndex) {
        requireSize(words, nameIndex + 2);
        if (!is(words.get(nameIndex), "tableau", "table", "t")) {
            throw new IllegalArgumentException("expected a tableau destination");
        }
        return parseColumn(words.get(nameIndex + 1));
    }

    private static int parseColumn(String text) {
        int humanColumn = positiveNumber(text, "tableau column");
        if (humanColumn > KlondikeGame.TABLEAU_COUNT) {
            throw new IllegalArgumentException("tableau column must be between 1 and 7");
        }
        return humanColumn - 1;
    }

    private static int positiveNumber(String text, String label) {
        try {
            int number = Integer.parseInt(text);
            if (number < 1) {
                throw new IllegalArgumentException(label + " must be positive");
            }
            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a number");
        }
    }

    private static void requireSize(List<String> words, int size) {
        if (words.size() < size) {
            throw new IllegalArgumentException("move is missing an argument");
        }
    }

    private void report(MoveResult result) {
        System.out.println((result.success() ? "OK: " : "No: ") + result.message());
        System.out.print(renderer.render(game));
        if (game.isWon()) {
            System.out.println("Congratulations — you won!");
        }
    }

    private static KlondikeGame parseSeed(String[] args) {
        if (args.length == 0) {
            return new KlondikeGame();
        }
        if (args.length == 2 && "--seed".equals(args[0])) {
            try {
                return new KlondikeGame(Long.parseLong(args[1]));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Seed must be a whole number.", exception);
            }
        }
        throw new IllegalArgumentException("Usage: CommandLineSolitaire [--seed NUMBER]");
    }

    private static boolean is(String actual, String... choices) {
        for (String choice : choices) {
            if (actual.equals(choice)) {
                return true;
            }
        }
        return false;
    }
}
