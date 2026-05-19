/*
@author Benjamin Le
 */

public class Card {
    private int rank;
    private char suit;
    public Card(int rank, char suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public char getSuit() {
        return suit;
    }

    public void setSuit(char suit) {
        this.suit = suit;
    }
    public boolean isRed () {
        return suit == '♥' || suit == '♦';
    }

    @Override
    public String toString() {
        if(rank == 1) {
            return "[A"+suit+"]";
        }
        if(rank == 11) {
            return "[J"+suit+"]";
        }
        if(rank == 12) {
            return "[Q"+suit+"]";
        }
        if(rank == 13) {
            return "[K"+suit+"]";
        }
        else {
            return "["+rank+suit+"]";
        }
    }
}
