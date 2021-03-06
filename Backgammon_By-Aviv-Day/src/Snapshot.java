
import java.util.LinkedList;
import java.util.List;

/*
 *
 * A Snapshot describes a snapshot of a backgammon board.
 *
 * Such a snapshot can be saved to disk.
 * It can be also loaded to continue a game.
 * It can be transmitted and received on a single line.
 *
 * transmitted data is:
 *    0-23 plates:  [i] > 0 ==> white[i+1] = [i]
 *                [i] < 0 ==> black[24-i] = [i]
 *    24 : white[25]
 *    25 : black[25]
 *    27 : white's turn?
 *    28 : the dice  (0-35)
 *
 * File Format:
 *
 * #.... are ignored
 * empty lines are ignored
 *
 * 1. white board
 * 2. black board
 * 3. whose turn line
 * 4. dice line
 *
 *
 * The following data is stored locally but neither transmitted over the
 * connection nor saved to / read from a file
 *  History
 *
 * @author Aviv
 * @version 1.0
 */
public class Snapshot {

    // stores all the data!
    private int[] whiteBoard = new int[26];
    private int[] blackBoard = new int[26];

    private boolean whitesTurn;
    private int[] dice;

    private List history;

    /*
     * take a snapshot from a gameController
     * @param game GameController to snapshoot
     */
    public Snapshot(GameController game) {
        whiteBoard = game.getWhite().getBoardGame();
        blackBoard = game.getBlack().getBoardGame();
        whitesTurn = (game.getCurPlayer() == game.getWhite());
        dice = game.getDice();
        history = new LinkedList(game.getHistory());
    }


    public Player getCurrentPlayer(Player white, Player black) {
        return whitesTurn ? white : black;
    }

    public int[] getWhiteBoard() {
        return whiteBoard;
    }

    public int[] getBlackBoard() {
        return blackBoard;
    }

    public List getHistory() {
        return history;
    }

    public int[] getDice() {
        return dice;
    }

    /*
     * Indicates whether this snapshot is equal to a different one.
     *
     * Snapshots are equal if:
     * - boards are equally setup.
     * - player in turn are equal
     * - dice are equal
     *
     * @param obj the snapshot object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *   argument; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        Snapshot other = (Snapshot)obj;
        return (intArrayEqual(this.getBlackBoard(), other.getBlackBoard()) &&
                intArrayEqual(this.getWhiteBoard(), other.getWhiteBoard()) &&
                this.whitesTurn == other.whitesTurn &&
                intArrayEqual(this.getDice(), other.getDice()));
    }

    private boolean intArrayEqual(int[]a, int[]b) {
        if(b.length != a.length)
            return false;

        for (int i = 0; i < a.length; i++) {
            if(a[i] != b[i])
                return false;
        }

        return true;
    }

}
