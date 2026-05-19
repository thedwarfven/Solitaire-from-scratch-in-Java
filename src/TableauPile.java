import java.util.ArrayList;

public class TableauPile extends Pile {
    private int faceDown;
    public TableauPile(ArrayList<Card> cards, int faceDown) {
        this.cards.addAll(cards);
        this.faceDown = faceDown;
    }
    @Override
    public void addCard(Card card) {
        if (cards.getLast().isRed()!=card.isRed()&&cards.getLast().getRank()>card.getRank()) {
            cards.add(card);
        }
        else {
            System.out.println("illegal move");
        }

    }
    public void addCards(ArrayList<Card> cards2) {
        if (cards.getLast().isRed()!=cards2.getFirst().isRed()&&cards.getLast().getRank()>cards2.getFirst().getRank()) {
            this.cards.addAll(cards2);
        }
        else {
            System.out.println("illegal move");
        }


    }
    public void tickCheck(){
        if(this.cards.size() == faceDown){
            faceDown--;
        }
        for(int i=0; i<cards.size(); i++){
            if(i<faceDown){
                cards.get(i).setHidden(true);
            }
             else {cards.get(i).setHidden(false);}
        }
    }


}
