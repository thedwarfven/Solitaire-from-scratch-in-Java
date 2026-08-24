package com.benjaminle.solitaire.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Complete, interface-independent rules engine for draw-one Klondike.
 *
 * <p>The end of each list is the top of that pile. Public accessors return
 * immutable copies, preventing a UI from accidentally bypassing move rules.
 * All positions exposed to callers are zero-based; the CLI converts its
 * human-friendly one-based column numbers before calling this class.</p>
 */
public final class KlondikeGame {
    public static final int TABLEAU_COUNT = 7;

    private final Random random;
    private final List<Card> stock = new ArrayList<>();
    private final List<Card> waste = new ArrayList<>();
    private final List<List<Card>> tableau = new ArrayList<>();
    private final EnumMap<Suit, List<Card>> foundations = new EnumMap<>(Suit.class);

    /** Creates and deals a new game using a fresh random seed. */
    public KlondikeGame() {
        this(new Random());
    }

    /**
     * Creates a deterministic game. This constructor is useful for tests,
     * tutorials, and reproducing a particular deal.
     */
    public KlondikeGame(long seed) {
        this(new Random(seed));
    }

    private KlondikeGame(Random random) {
        this.random = Objects.requireNonNull(random, "random");
        newGame();
    }

    /**
     * Creates a controlled state for engine tests in this package. Keeping this
     * constructor package-private prevents production interfaces from bypassing
     * the normal shuffled deal while allowing tests to describe rules clearly.
     */
    KlondikeGame(List<Card> stock, List<Card> waste, List<List<Card>> tableau,
                  Map<Suit, List<Card>> foundations) {
        this.random = new Random(0);
        this.stock.addAll(stock);
        this.waste.addAll(waste);
        if (tableau.size() != TABLEAU_COUNT) {
            throw new IllegalArgumentException("A test state must have seven tableau columns.");
        }
        for (List<Card> column : tableau) {
            this.tableau.add(new ArrayList<>(column));
        }
        for (Suit suit : Suit.values()) {
            this.foundations.put(suit,
                    new ArrayList<>(foundations.getOrDefault(suit, List.of())));
        }
    }

    /** Clears all piles, shuffles a fresh deck, and deals the tableau. */
    public void newGame() {
        stock.clear();
        waste.clear();
        tableau.clear();
        foundations.clear();

        for (Suit suit : Suit.values()) {
            foundations.put(suit, new ArrayList<>());
        }
        for (int column = 0; column < TABLEAU_COUNT; column++) {
            tableau.add(new ArrayList<>());
        }

        List<Card> deck = createDeck();
        Collections.shuffle(deck, random);

        // Klondike deals one card to column one, two to column two, and so on.
        for (int column = 0; column < TABLEAU_COUNT; column++) {
            for (int row = 0; row <= column; row++) {
                Card card = removeTop(deck);
                card.setFaceUp(row == column);
                tableau.get(column).add(card);
            }
        }
        for (Card card : deck) {
            card.setFaceUp(false);
            stock.add(card);
        }
    }

    private static List<Card> createDeck() {
        List<Card> deck = new ArrayList<>(52);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(rank, suit, false));
            }
        }
        return deck;
    }

    /**
     * Draws one card, or restocks when the stock is empty.
     * Restocks are deliberately unlimited, as selected for this project.
     */
    public MoveResult draw() {
        if (!stock.isEmpty()) {
            Card card = removeTop(stock);
            card.setFaceUp(true);
            waste.add(card);
            return MoveResult.success("Drew " + card.shortName() + ".");
        }
        if (waste.isEmpty()) {
            return MoveResult.failure("Both stock and waste are empty.");
        }
        while (!waste.isEmpty()) {
            Card card = removeTop(waste);
            card.setFaceUp(false);
            stock.add(card);
        }
        return MoveResult.success("Restocked the deck.");
    }

    /** Moves the waste's top card to a tableau column. */
    public MoveResult moveWasteToTableau(int targetColumn) {
        MoveResult targetCheck = validateColumn(targetColumn);
        if (!targetCheck.success()) {
            return targetCheck;
        }
        if (waste.isEmpty()) {
            return MoveResult.failure("The waste is empty.");
        }
        Card card = top(waste);
        if (!canPlaceOnTableau(card, tableau.get(targetColumn))) {
            return MoveResult.failure(tableauRuleMessage(card, targetColumn));
        }
        tableau.get(targetColumn).add(removeTop(waste));
        return MoveResult.success("Moved " + card.shortName() + " to tableau " + (targetColumn + 1) + ".");
    }

    /** Moves the waste's top card to its suit's foundation. */
    public MoveResult moveWasteToFoundation() {
        if (waste.isEmpty()) {
            return MoveResult.failure("The waste is empty.");
        }
        Card card = top(waste);
        if (!canPlaceOnFoundation(card)) {
            return MoveResult.failure("Foundation cards must ascend from Ace to King in the same suit.");
        }
        foundations.get(card.suit()).add(removeTop(waste));
        return MoveResult.success("Moved " + card.shortName() + " to its foundation.");
    }

    /** Moves the exposed top card of a tableau column to its foundation. */
    public MoveResult moveTableauToFoundation(int sourceColumn) {
        MoveResult sourceCheck = validateColumn(sourceColumn);
        if (!sourceCheck.success()) {
            return sourceCheck;
        }
        List<Card> source = tableau.get(sourceColumn);
        if (source.isEmpty()) {
            return MoveResult.failure("That tableau column is empty.");
        }
        Card card = top(source);
        if (!card.isFaceUp() || !canPlaceOnFoundation(card)) {
            return MoveResult.failure("That card cannot be placed on its foundation.");
        }
        foundations.get(card.suit()).add(removeTop(source));
        revealTop(source);
        return MoveResult.success("Moved " + card.shortName() + " to its foundation.");
    }

    /**
     * Moves a face-up run between tableau columns.
     *
     * @param sourceColumn zero-based source column
     * @param startIndex zero-based index of the first card in the run
     * @param targetColumn zero-based destination column
     */
    public MoveResult moveTableauToTableau(int sourceColumn, int startIndex, int targetColumn) {
        MoveResult sourceCheck = validateColumn(sourceColumn);
        if (!sourceCheck.success()) {
            return sourceCheck;
        }
        MoveResult targetCheck = validateColumn(targetColumn);
        if (!targetCheck.success()) {
            return targetCheck;
        }
        if (sourceColumn == targetColumn) {
            return MoveResult.failure("Source and destination must be different columns.");
        }

        List<Card> source = tableau.get(sourceColumn);
        if (startIndex < 0 || startIndex >= source.size()) {
            return MoveResult.failure("The selected card does not exist.");
        }
        List<Card> moving = new ArrayList<>(source.subList(startIndex, source.size()));
        if (!isValidFaceUpRun(moving)) {
            return MoveResult.failure("Only a face-up, descending, alternating-color run may move.");
        }
        Card first = moving.get(0);
        if (!canPlaceOnTableau(first, tableau.get(targetColumn))) {
            return MoveResult.failure(tableauRuleMessage(first, targetColumn));
        }

        source.subList(startIndex, source.size()).clear();
        tableau.get(targetColumn).addAll(moving);
        revealTop(source);
        return MoveResult.success("Moved " + moving.size() + " card(s) to tableau " + (targetColumn + 1) + ".");
    }

    /** Moves a foundation's top card back to a tableau column. */
    public MoveResult moveFoundationToTableau(Suit suit, int targetColumn) {
        Objects.requireNonNull(suit, "suit");
        MoveResult targetCheck = validateColumn(targetColumn);
        if (!targetCheck.success()) {
            return targetCheck;
        }
        List<Card> foundation = foundations.get(suit);
        if (foundation.isEmpty()) {
            return MoveResult.failure("The " + suit.name().toLowerCase() + " foundation is empty.");
        }
        Card card = top(foundation);
        if (!canPlaceOnTableau(card, tableau.get(targetColumn))) {
            return MoveResult.failure(tableauRuleMessage(card, targetColumn));
        }
        tableau.get(targetColumn).add(removeTop(foundation));
        return MoveResult.success("Moved " + card.shortName() + " back to the tableau.");
    }

    /** Returns true after all 52 cards reach the foundations. */
    public boolean isWon() {
        return foundations.values().stream().mapToInt(List::size).sum() == 52;
    }

    public int stockSize() {
        return stock.size();
    }

    public Optional<Card> wasteTop() {
        return waste.isEmpty() ? Optional.empty() : Optional.of(top(waste));
    }

    public List<Card> tableau(int column) {
        validateColumnOrThrow(column);
        return List.copyOf(tableau.get(column));
    }

    public List<Card> foundation(Suit suit) {
        return List.copyOf(foundations.get(Objects.requireNonNull(suit, "suit")));
    }

    public Map<Suit, List<Card>> foundations() {
        EnumMap<Suit, List<Card>> copy = new EnumMap<>(Suit.class);
        foundations.forEach((suit, cards) -> copy.put(suit, List.copyOf(cards)));
        return Collections.unmodifiableMap(copy);
    }

    private boolean canPlaceOnFoundation(Card card) {
        List<Card> foundation = foundations.get(card.suit());
        int requiredRank = foundation.size() + 1;
        return card.isFaceUp() && card.rank().value() == requiredRank;
    }

    private static boolean canPlaceOnTableau(Card card, List<Card> target) {
        if (!card.isFaceUp()) {
            return false;
        }
        if (target.isEmpty()) {
            return card.rank() == Rank.KING;
        }
        Card targetTop = top(target);
        return targetTop.isFaceUp()
                && targetTop.isRed() != card.isRed()
                && targetTop.rank().value() == card.rank().value() + 1;
    }

    private static boolean isValidFaceUpRun(List<Card> cards) {
        if (cards.isEmpty() || !cards.get(0).isFaceUp()) {
            return false;
        }
        for (int index = 1; index < cards.size(); index++) {
            Card previous = cards.get(index - 1);
            Card current = cards.get(index);
            if (!current.isFaceUp()
                    || previous.isRed() == current.isRed()
                    || previous.rank().value() != current.rank().value() + 1) {
                return false;
            }
        }
        return true;
    }

    private static void revealTop(List<Card> pile) {
        if (!pile.isEmpty()) {
            top(pile).setFaceUp(true);
        }
    }

    private MoveResult validateColumn(int column) {
        if (column < 0 || column >= TABLEAU_COUNT) {
            return MoveResult.failure("Tableau column must be between 1 and 7.");
        }
        return MoveResult.success("");
    }

    private void validateColumnOrThrow(int column) {
        if (column < 0 || column >= TABLEAU_COUNT) {
            throw new IndexOutOfBoundsException("Tableau column must be between 0 and 6: " + column);
        }
    }

    private static String tableauRuleMessage(Card card, int targetColumn) {
        return "Cannot place " + card.shortName() + " on tableau " + (targetColumn + 1)
                + "; build down by alternating colors, with only Kings on empty columns.";
    }

    private static Card top(List<Card> cards) {
        return cards.get(cards.size() - 1);
    }

    private static Card removeTop(List<Card> cards) {
        return cards.remove(cards.size() - 1);
    }
}
