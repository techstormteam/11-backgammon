

import java.util.*;

/**
 * A move of on chip on the board.
 *
 * Each move goes from a plate to a plate.
 * Possible values are 0-25.
 * The length is the difference between from() and to()
 *
 * @author Aviv
 */
public interface Move extends PlayerObject, Comparable {

    /**
     * a moves starts at a plate or at the bar
     * @return integer between 1 and 25.
     */
    public int fromPlate();

    /**
     * a move ends at a plate or in the off
     * @return integer between 0 and 24.
     */
    public int toPlate();

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
    public int getOneMovesCount();

    /**
     * a move may be composed of several basic moves, this methods returns these
     * basic moves
     * @return List of Move-Objets
     */
    public List getOneMoves();
}
