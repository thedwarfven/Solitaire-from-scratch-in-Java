import java.util.ArrayList;

public class Pile {
    protected ArrayList<Card> cards = new ArrayList<Card>();
    public Pile() {
    }
    public Card getLastCard() {
        return cards.getLast();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card dealCard() {
        Card result = cards.getLast();
        cards.remove(result);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Card card : cards) {
            result.append(card.toString()).append(" ");
        }
        return result.toString();
    }
}
