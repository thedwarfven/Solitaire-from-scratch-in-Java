import java.util.ArrayList;

public class TableauPileTester {
    public static void main(String[] args) {
        Deck deck = new Deck();
        deck.shuffle();
        ArrayList<Card> tester = new ArrayList<Card>();
        for(int i = 0; i < 5; i++){
            tester.add(deck.dealCard());
        }
        TableauPile t1 = new TableauPile(tester,4);
        t1.tickCheck();
        System.out.println(t1.toString());
        t1.dealCard();
        t1.tickCheck();
        System.out.println(t1.toString());

    }
}
