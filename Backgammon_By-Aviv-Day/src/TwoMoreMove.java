

import java.util.Collections;
import java.util.List;
import java.util.*;

/*
 * A MultiMove is a move that is composed of two Move-Objects m1 and m2.
 *
 * But m1.to = m2.from must always be true!
 *
 * Both m1 or m2 may be MultiMove-Objects themselves.
 *
 * @author Aviv
 */
public class TwoMoreMove implements Move {

    private Move move1, move2;

    public TwoMoreMove(Move m1, Move m2) {
        if (m1.toPlate() != m2.fromPlate()) {
            throw new IllegalArgumentException("m2 does not continue m2: " + m1 +
                                               " " + m2);
        }
        //assert m1.player()==m2.player();
        move1 = m1;
        move2 = m2;
    }

    public void setPlayer(Player player) {
        move1.setPlayer(player);
        move2.setPlayer(player);
    }

    /*
     * get the SingleMoves of which this move is compound
     */
    public List getOneMoves() {
        List ret = new LinkedList(move1.getOneMoves());
        ret.addAll(move2.getOneMoves());
        return ret;
    }

    public int fromPlate() {
        return move1.fromPlate();
    }

    public int toPlate() {
        return move2.toPlate();
    }

    public Player player() {
        return move1.player();
    }

    public String toString() {
        String ret = ""+move1.fromPlate();
        OneMove item = null;
        for (Iterator iter = getOneMoves().iterator(); iter.hasNext(); ) {
            item = (OneMove) iter.next();
            ret += "/" + item.toPlate();
        }
        /* @todo append "*" if beat */
        return ret;
    }

    public int length() {
        return fromPlate()-toPlate();
    }

    public int getOneMovesCount() {
        return move1.getOneMovesCount() + move2.getOneMovesCount();
    }

    // sorting moves according to the number of hops
    public int compareTo(Object o) {
        return getOneMovesCount() - ((Move)o).getOneMovesCount();
    }

}
