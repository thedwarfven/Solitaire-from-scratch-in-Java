package com.benjaminle.solitaire.cli;

import com.benjaminle.solitaire.engine.Card;
import com.benjaminle.solitaire.engine.KlondikeGame;
import com.benjaminle.solitaire.engine.Suit;

import java.util.ArrayList;
import java.util.List;

/** Converts the game state into a compact, readable terminal board. */
final class ConsoleRenderer {
    String render(KlondikeGame game) {
        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator());
        output.append("Stock: ").append(game.stockSize() == 0 ? "[  ]" : "[XX]");
        output.append("  Waste: ").append(game.wasteTop().map(Card::toString).orElse("[  ]"));
        output.append("     Foundations: ");
        for (Suit suit : Suit.values()) {
            List<Card> pile = game.foundation(suit);
            output.append(suit.symbol()).append(':')
                    .append(pile.isEmpty() ? "[  ]" : pile.get(pile.size() - 1))
                    .append(' ');
        }
        output.append(System.lineSeparator()).append(System.lineSeparator());
        output.append("  T1    T2    T3    T4    T5    T6    T7").append(System.lineSeparator());

        List<List<Card>> columns = new ArrayList<>();
        int tallest = 0;
        for (int column = 0; column < KlondikeGame.TABLEAU_COUNT; column++) {
            List<Card> cards = game.tableau(column);
            columns.add(cards);
            tallest = Math.max(tallest, cards.size());
        }
        for (int row = 0; row < tallest; row++) {
            for (List<Card> column : columns) {
                String cell = row < column.size() ? column.get(row).toString() : "";
                output.append(String.format("%-6s", cell));
            }
            output.append(System.lineSeparator());
        }
        return output.toString();
    }
}
