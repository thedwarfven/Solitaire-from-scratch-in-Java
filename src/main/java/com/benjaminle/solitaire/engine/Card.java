package com.benjaminle.solitaire.engine;

import java.util.Objects;

/**
 * A standard playing card.
 *
 * <p>Rank and suit never change. Face orientation is mutable because a tableau
 * card is revealed as play progresses and stock cards flip during a restock.</p>
 */
public final class Card {
    private final Rank rank;
    private final Suit suit;
    private boolean faceUp;

    public Card(Rank rank, Suit suit, boolean faceUp) {
        this.rank = Objects.requireNonNull(rank, "rank");
        this.suit = Objects.requireNonNull(suit, "suit");
        this.faceUp = faceUp;
    }

    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
    }

    public boolean isRed() {
        return suit.isRed();
    }

    /** A compact label suitable for both terminal and graphical displays. */
    public String shortName() {
        return rank.label() + suit.symbol();
    }

    @Override
    public String toString() {
        return faceUp ? "[" + shortName() + "]" : "[XX]";
    }
}
