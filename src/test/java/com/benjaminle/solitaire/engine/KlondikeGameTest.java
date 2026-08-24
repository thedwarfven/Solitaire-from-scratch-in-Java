package com.benjaminle.solitaire.engine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KlondikeGameTest {
    @Test
    void newGameDealsStandardKlondikeLayout() {
        KlondikeGame game = new KlondikeGame(42);

        assertEquals(24, game.stockSize());
        int totalCards = game.stockSize();
        for (int column = 0; column < KlondikeGame.TABLEAU_COUNT; column++) {
            List<Card> cards = game.tableau(column);
            assertEquals(column + 1, cards.size());
            totalCards += cards.size();

            for (int index = 0; index < cards.size(); index++) {
                assertEquals(index == cards.size() - 1, cards.get(index).isFaceUp());
            }
        }
        assertEquals(52, totalCards);
    }

    @Test
    void sameSeedProducesSameDeal() {
        KlondikeGame first = new KlondikeGame(12345);
        KlondikeGame second = new KlondikeGame(12345);

        for (int column = 0; column < KlondikeGame.TABLEAU_COUNT; column++) {
            assertEquals(names(first.tableau(column)), names(second.tableau(column)));
        }
    }

    @Test
    void restockPreservesOriginalDrawOrderAndCanRepeat() {
        Card ace = card(Rank.ACE, Suit.SPADES, false);
        Card two = card(Rank.TWO, Suit.HEARTS, false);
        KlondikeGame game = state(List.of(ace, two), List.of(), emptyTableau(), Map.of());

        assertTrue(game.draw().success());
        assertEquals("2\u2665", game.wasteTop().orElseThrow().shortName());
        assertTrue(game.draw().success());
        assertEquals("A\u2660", game.wasteTop().orElseThrow().shortName());
        assertTrue(game.draw().success()); // Restock.
        assertEquals(2, game.stockSize());
        assertTrue(game.draw().success());
        assertEquals("2\u2665", game.wasteTop().orElseThrow().shortName());
    }

    @Test
    void movesAValidTableauRunAndRevealsNewTop() {
        Card hiddenNine = card(Rank.NINE, Suit.SPADES, false);
        Card redEight = card(Rank.EIGHT, Suit.HEARTS, true);
        Card blackSeven = card(Rank.SEVEN, Suit.CLUBS, true);
        Card blackNine = card(Rank.NINE, Suit.CLUBS, true);
        List<List<Card>> tableau = emptyTableau();
        tableau.set(0, new ArrayList<>(List.of(hiddenNine, redEight, blackSeven)));
        tableau.set(1, new ArrayList<>(List.of(blackNine)));
        KlondikeGame game = state(List.of(), List.of(), tableau, Map.of());

        MoveResult result = game.moveTableauToTableau(0, 1, 1);

        assertTrue(result.success());
        assertEquals(1, game.tableau(0).size());
        assertTrue(game.tableau(0).get(0).isFaceUp());
        assertEquals(List.of("9\u2663", "8\u2665", "7\u2663"), names(game.tableau(1)));
    }

    @Test
    void rejectsSameColorOrNonDescendingTableauMoves() {
        List<List<Card>> tableau = emptyTableau();
        tableau.set(0, new ArrayList<>(List.of(card(Rank.EIGHT, Suit.DIAMONDS, true))));
        tableau.set(1, new ArrayList<>(List.of(card(Rank.NINE, Suit.HEARTS, true))));
        KlondikeGame game = state(List.of(), List.of(), tableau, Map.of());

        assertFalse(game.moveTableauToTableau(0, 0, 1).success());
        assertEquals(1, game.tableau(0).size());
        assertEquals(1, game.tableau(1).size());
    }

    @Test
    void onlyKingsCanFillEmptyTableauColumns() {
        KlondikeGame queenGame = state(List.of(),
                List.of(card(Rank.QUEEN, Suit.CLUBS, true)), emptyTableau(), Map.of());
        assertFalse(queenGame.moveWasteToTableau(0).success());

        KlondikeGame kingGame = state(List.of(),
                List.of(card(Rank.KING, Suit.CLUBS, true)), emptyTableau(), Map.of());
        assertTrue(kingGame.moveWasteToTableau(0).success());
    }

    @Test
    void foundationsBuildBySuitFromAceAndAllowRollback() {
        Card ace = card(Rank.ACE, Suit.HEARTS, true);
        Card two = card(Rank.TWO, Suit.HEARTS, true);
        KlondikeGame game = state(List.of(), List.of(ace, two), emptyTableau(), Map.of());

        assertFalse(game.moveWasteToFoundation().success());
        assertEquals("2\u2665", game.wasteTop().orElseThrow().shortName());

        KlondikeGame aceGame = state(List.of(), List.of(ace), emptyTableau(), Map.of());
        assertTrue(aceGame.moveWasteToFoundation().success());
        assertEquals(1, aceGame.foundation(Suit.HEARTS).size());

        List<List<Card>> target = emptyTableau();
        target.set(0, new ArrayList<>(List.of(card(Rank.TWO, Suit.CLUBS, true))));
        KlondikeGame rollback = state(List.of(), List.of(), target,
                Map.of(Suit.HEARTS, List.of(card(Rank.ACE, Suit.HEARTS, true))));
        assertTrue(rollback.moveFoundationToTableau(Suit.HEARTS, 0).success());
    }

    @Test
    void publicPileViewsCannotBeMutated() {
        KlondikeGame game = new KlondikeGame(7);
        assertThrows(UnsupportedOperationException.class,
                () -> game.tableau(0).add(card(Rank.ACE, Suit.CLUBS, true)));
        assertThrows(UnsupportedOperationException.class,
                () -> game.foundation(Suit.CLUBS).clear());
    }

    private static KlondikeGame state(List<Card> stock, List<Card> waste,
                                       List<List<Card>> tableau,
                                       Map<Suit, List<Card>> foundations) {
        return new KlondikeGame(stock, waste, tableau, foundations);
    }

    private static List<List<Card>> emptyTableau() {
        List<List<Card>> result = new ArrayList<>();
        for (int index = 0; index < KlondikeGame.TABLEAU_COUNT; index++) {
            result.add(new ArrayList<>());
        }
        return result;
    }

    private static Card card(Rank rank, Suit suit, boolean faceUp) {
        return new Card(rank, suit, faceUp);
    }

    private static List<String> names(List<Card> cards) {
        return cards.stream().map(Card::shortName).toList();
    }
}
