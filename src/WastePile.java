import java.util.Collections;

public class WastePile extends Pile {
    public WastePile() {
    }
    public void shuffle() {
        Collections.shuffle(cards);
    }
}
