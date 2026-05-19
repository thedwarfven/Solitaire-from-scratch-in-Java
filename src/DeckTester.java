/*
@author Benjamin Le
 */

public class DeckTester {
    public static void main(String[] args) {
        Deck deck = new Deck();
        deck.shuffle();
        System.out.println(deck.toString());
        for(int i = 0; i < 5; i++){
            System.out.println(deck.dealCard());
        }
        System.out.println(deck.toString());

    }
}
