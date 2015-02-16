 
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Objects of class Move describe a Move in the Backgammon game.
 *
 * Normally this is the act of moving a chip from one plate to another.
 * For logging reasons this may also br an arbitrary String associated with
 * a user.
 *
 * Normal moves can represented as Text. This is in the form
 *     from/to(*)
 * where from and to are plates. The * is appended if a chip is thrown out
 * of the game.
 *
 * Other notational abbreviations are allowed but not yet implemented.
 *
 * @author Aviv
 */
public class OneMove implements Move {

    private int fromPlate;
    private int toPlate;
    private boolean beat;
    private Player player;

    public OneMove(Player player, int from, int to, boolean beat) {
        this.player = player;
        this.fromPlate = from;
        this.toPlate = to;
        this.beat = beat;
    }

    public OneMove(Player player, int from, int to) {
        this(player,from,to,false);
    }

    public String toString() {
        return "" + fromPlate + "/" + toPlate + (beat ? "*" : "");
    }

    public int fromPlate() {
        return fromPlate;
    }

    public int toPlate() {
        return toPlate;
    }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setBeat(boolean b) {
        beat = b;
    }

    /**
     * get the moves of which this move is compound
     */
    public List getOneMoves() {
        return Collections.singletonList(this);
    }

    public Player player() {
        return player;
    }

    public int length() {
        return fromPlate-toPlate;
    }

    public int getOneMovesCount() {
        return 1;
    }

    // sorting moves according to the number of hops
    public int compareTo(Object o) {
        return getOneMovesCount() - ((Move)o).getOneMovesCount();
    }

}
