/*
@author Benjamin Le
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collections.*;
public class Deck {
    private ArrayList<Card> cards = new ArrayList<Card>();
    public Deck() {
        for (int i = 0; i <= 13; i++) {
            Card spades = new Card(i,'\u2660');
            Card clubs = new Card(i,'\u2663');
            Card diamonds = new Card(i,'\u2666');
            Card hearts = new Card(i,'\u2665');
            cards.add(spades);
            cards.add(clubs);
            cards.add(diamonds);
            cards.add(hearts);
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        return cards.getFirst();
    }

}
