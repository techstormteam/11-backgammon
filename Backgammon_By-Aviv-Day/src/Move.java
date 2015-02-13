

import java.util.*;

/**
 * A move of on chip on the board.
 *
 * Each move goes from a jag to a jag.
 * Possible values are 0-25.
 * The length is the difference between from() and to()
 *
 * @author Aviv
 */
public interface Move extends PlayerOwnedObject, Comparable {

    /**
     * a moves starts at a jag or at the bar
     * @return integer between 1 and 25.
     */
    public int from();

    /**
     * a move ends at a jag or in the off
     * @return integer between 0 and 24.
     */
    public int to();

    /**
     * length of this move. The condition length()=from()-to() is always true.
     * @return length of this move.
     */
    public int length();

    /**
     * a move may be composed of several basic moves, this methods returns how
     * many basic moves are composed
     * @return number of basic moves in this move
     */
    public int getSingleMovesCount();

    /**
     * a move may be composed of several basic moves, this methods returns these
     * basic moves
     * @return List of Move-Objets
     */
    public List getSingleMoves();
}
