package com.benjaminle.solitaire.engine;

/** The four French-suited playing-card suits used by Klondike. */
public enum Suit {
    CLUBS('\u2663', false),
    DIAMONDS('\u2666', true),
    HEARTS('\u2665', true),
    SPADES('\u2660', false);

    private final char symbol;
    private final boolean red;

    Suit(char symbol, boolean red) {
        this.symbol = symbol;
        this.red = red;
    }

    public char symbol() {
        return symbol;
    }

    public boolean isRed() {
        return red;
    }

    /**
     * Parses either a full suit name, its first letter, or its Unicode symbol.
     *
     * @param text user-provided suit text
     * @return the matching suit
     * @throws IllegalArgumentException when the text does not name a suit
     */
    public static Suit parse(String text) {
        String normalized = text.trim().toLowerCase();
        return switch (normalized) {
            case "c", "club", "clubs", "\u2663" -> CLUBS;
            case "d", "diamond", "diamonds", "\u2666" -> DIAMONDS;
            case "h", "heart", "hearts", "\u2665" -> HEARTS;
            case "s", "spade", "spades", "\u2660" -> SPADES;
            default -> throw new IllegalArgumentException("Unknown suit: " + text);
        };
    }
}
