
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class captures a player(party) involved in the backgammon game.
 *
 * Each player knows about its chips in the game.
 * Can thus calculate possible moves and check whether a move is valid or not.
 *
 * Some behaviour is different between remote and local players so this class is
 * abstract.
 *
 * @author Aviv
 */
public abstract class Player {

    public static final int ORDINARY = 1;

    public static final int ROLL = 0;
    private String playerName;
    private GameController gameController;
    private int displayedDice[];

    // 0:off  1:24 board  25:bar
    private int boardGame[];
    // one chip can be dragged arround
    // this is substracted for the the draggingQueries!
    private int draggingAround;
    private IntegerList remainingHops;
    // number of chips placed during iniboard

    public Player(String name) {
        this.playerName = name;
        createGame();
    }

    public Player() {
        createGame();
    }

    /**
     * sets the Game.
     * @param game Game
     * @throws happens in NetworkPlayer
     */
    public void setGameController(GameController game) throws IOException {
        this.gameController = game;
    }

    public Player getRemainingPlayer() {
        return gameController.getRemainingPlayer(this);
    }

    /**
     * initial position of the chips.
     *
     * Can be changed for debug purposes with system property
     */
    private void createGame() {
        String sys = System.getProperty("jgam.initialboard");
        boardGame = new int[26];

        if (sys == null) {
            boardGame[24] = 2;
            boardGame[13] = 5;
            boardGame[8] = 3;
            boardGame[6] = 5;
        } else {
            int total = 0;
            for (int i = 1; i < boardGame.length; i++) {
                boardGame[i] = (int) (sys.charAt(i - 1) - '0');
                total += boardGame[i];
            }
            boardGame[0] = 15 - total;
        }
    }

    /**
     * call this with care!
     */
    public void setBoard(int[] newBoard) {
        assert newBoard.length == 26;
        boardGame = (int[])newBoard.clone();
    }

    public int getPlate(int pos) {
        return boardGame[pos];
    }

    /**
     * get the content of a plate.
     * If one of these is currently dragged around (UI)
     * one less is returned
     * @param pos plate number
     * @return number of chips on this plate
     */
    public int getPlateWithDragging(int pos) {
        if (pos == draggingAround) {
            return getPlate(pos) - 1;
        } else {
            return getPlate(pos);
        }
    }

    /**
     * is at this plate a single chip that can be thrown out?
     * @param i plateindex
     * @return true iff there is
     */
    private boolean isPlot(int i) {
        return getPlate(i) == 1;
    }

    /**
     * check whether all are at home.
     * I can start playing out by then.
     * @return true iff there are no chips on plate 7 - 25.
     */
    public boolean areAllAtHome() {
        return maxPlate() <= 6;
    }

    public int[] getShownDice() {
        return displayedDice;
    }


    /**
     * can i still make a move with the remaining dice-moves.
     *
     * To be able to make a move, it suffices to be able to
     * move from one plate one length.
     *
     * @return true iff I can still move
     */
    public boolean canMove() {
        IntegerList dist = remainingHops.distinctValues();
        for (int i = 0; i < dist.length(); i++) {
            for (int plate = 1; plate <= 25; plate++) {
                if (validMove(plate, dist.at(i))) {
                    return true;
                }
                if (validMove(plate, plate)) { // play out
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * is a move from a plate with a given length possible.
     * The remaininging steps are taken into consideration.
     * If all are at home then steps can be used to play out
     * @param fromPlate plate to start at
     * @param length length ot the move
     * @return true iff possible
     */
    public boolean validMove(int fromPlate, int length) {
        return isMovable(boardGame, fromPlate, length, remainingHops);
    }


    /**
     * get all moves that are possible from a specific startplate on.
     * The remaining steps are taken into consideration. A move may consist
     * of multiple hops.
     *
     * @param from plate to start at
     * @param List of Move objects
     */
    public List getPossibleMovesFrom(int from) {
        IntegerList rem = (IntegerList) remainingHops.clone();
        int[] brd = (int[])boardGame.clone();

        return getPossibleMovesFrom(from, brd, rem);
    }

    private List getPossibleMovesFrom(int from, int[] locBoard,
                                      IntegerList locRemaining) {
        IntegerList hops = locRemaining.distinctValues();
        List ret = new ArrayList();

        for (int i = 0; i < hops.length(); i++) {
            int length = hops.at(i);
            if (isMovable(locBoard, from, length, locRemaining)) {
                // change localBoard
                int to = Math.max(0, from-length);
                locRemaining.removeInteger(length);
                locBoard[from]--;
                locBoard[to]++;
                OneMove origMove = new OneMove(this, from, to);
                ret.add(origMove);
                for (Iterator iter = getPossibleMovesFrom(to,
                        locBoard,
                        locRemaining).iterator(); iter.hasNext(); ) {
                    Move move = (Move) iter.next();
                    ret.add(new TwoMoreMove(origMove, move));
                }
                // undo changes
                locRemaining.addInteger(length);
                locBoard[to]--;
                locBoard[from]++;
            }
        }
        return ret;
    }


    /**
     * checks for a given board whether a certain single hop can be made or not.
     *
     * 0. 1<=length<=6, from >= length
     * 1. 1<=from<=25 (not 0!) must have at least one chip of my colour
     * 2. if bar > 0 then from must be 25
     * 3. to(=from-length) may have at most one chip of the opponent.
     * 4. if to==0 then maxplate must be <= 6
     * 5. length must be in remainingMoves or maxPlate() == from and remainingMoves.max() > from
     *
     * The parameters got the prefix loc to distinguish them from the gloabal
     * values
     *
     * @param board int[] the board to check the move upon
     * @param remainingMoves the remaining moves
     * @return true if a move can be made
     */
    private boolean isMovable(int[] locBoard, int from, int length,
                            IntegerList locHops) {

        // correct the length if it left the board!
        length = Math.min(from, length);    // ensures length <= from
        int to = from - length;
        int maxPlate = 0;
        for (int i = 0; i < locBoard.length; i++) {
            if (locBoard[i] > 0) {
                maxPlate = i;
            }
        }

        // 0.
        if (length <= 0 || length > 6) {
            return false;
        }

        // 1.
        if (from <= 0 || from > 25) {
            return false;
        }

        if (locBoard[from] == 0) {
            return false;
        }

        // 2.
        if (locBoard[25] > 0 && from != 25) {
            return false;
        }

        // bugfix => goal == 0 ==> other[25] may be > 1 (0.9.24)
        // 3.
        if (to != 0 && getRemainingPlayer().getPlate(25 - to) > 1) {
            return false;
        }

        // 4.
        if (to == 0 && maxPlate > 6) {
            return false;
        }

        // 5.
        if (locHops.contains(length)) {
            return true;
        }

        if (from < 6 && maxPlate == from && locHops.max() > length) {
            return true;
        }

        return false;
    }

    public int getBar() {
        return boardGame[25];
    }

    public int getOff() {
        return boardGame[0];
    }


    /**
     * from the given dice deduce what move-lengths i could make.
     * doublets count 4 times! Store the result in remainingMoves.
     * @return IntList
     */
    public IntegerList setPossibleHops(int dice[]) {

        if (dice == null || dice.length != 2) {
            return remainingHops = null;
        }

        int[] steps = dice;

        if (dice[0] == dice[1]) { // doublets
            steps = new int[] {dice[0], dice[0], dice[0],
                    dice[0]};
        }

        remainingHops = new IntegerList(steps);

        return remainingHops;

    }


    /**
     * sets the dice that have been thrown and deduce the moves that can be made
     * with these.
     * @param dice int[]
     */
    public void setDice(int[] dice) {
        setDisplayedDice(dice);
        setPossibleHops(dice);
    }

    public void setDice(int a) {
        setDice(new int[] { a });
    }

    /**
     * sets the dice that are shown. The moves that can be made are NOT changed.
     */
    public void setDisplayedDice(int[] dice) {
        displayedDice = dice;
    }

    public String getName() {
        return playerName;
    }

    public void setPlayerName(String n) {
        playerName = n;
    }

    public String toString() {
        return getName();
    }

    /**
     * player 1 is white, player 2 is black
     * @return String "black" or "white"
     */
    public String getColorName() {
        return isWhite() ? "white" : "black";
    }

    public javax.swing.ImageIcon getChipIcon() {
        return isWhite() ? GuiOfBoard.whiteIcon : GuiOfBoard.blackIcon;
    }

    /**
     * a message is passed from the awtthread. This can be a move or a button
     * push. Only human players pay attention
     *
     * @param msg Message-Object
     */
    public abstract void handle(Object msg);

    /**
     * get the next Move.
     * This either single move, or a multi move
     */
    public abstract Move move() throws Exception;

    /**
     * if this player wants to doube or give up before his/her/move.
     * @return one of ROLL
     * @param rollOnly if true, only ROLL is permitted
     */
    public abstract int stepNext(boolean rollOnly) throws Exception;

    public int stepNext() throws Exception {
        return stepNext(false);
    }

    /**
     * tell this player that the opponent wants to throw the dice
     */
    abstract public void doRoll() throws Exception;

    /**
     * tell this player that the opponent has made a move
     * @param move the move made
     */
    abstract public void doMove(OneMove move) throws Exception;

    /**
     * free resources such as sockets ...
     */
    public void dispose() { }

    /**
     * tell this player whether the opponent accepted an offer or not
     * @param answer the answer to be told.
     */
    abstract public void doAccept(boolean answer) throws Exception;

    /**
     * are UI-moves to be made right now?
     * @return true if yes
     */
    abstract public boolean WaitingForUIMove();


    /**
     * a player has won when all the chips are in the plate 0
     * @return true iff this player has won
     */
    public boolean hasGameWon() {
        return getPlate(0) == 15;
    }

    /**
     * get the underlying game
     * @return game to which this belongs
     */
    public GameController getGame() {
        return gameController;
    }


    /**
     * apply a move to the data.
     * This move must be a single hop
     * @param m Move to be archived.
     */
    public void performMove(OneMove m) {

        if (!validMove(m.fromPlate(), m.length())) {
            throw new IllegalArgumentException("Illegal move " + m);
        }

        m.setPlayer(this);
        animateMove(m);
        if (getRemainingPlayer().isPlot(25 - m.toPlate())) {
            m.setBeat(true);
            getRemainingPlayer().discardAtPlate(25 - m.toPlate());
        }
        boardGame[m.fromPlate()]--;
        boardGame[m.toPlate()]++;
        if (!remainingHops.removeInteger(m.length())) {
            remainingHops.removeMax();
        }
    }

    /**
     * show the animation for this move. only remote players do
     * @param m Move to animate
     */
    abstract public void animateMove(Move m);

    /**
     * throw out a chip.
     * @param i plate to operate on
     */
    private void discardAtPlate(int i) {
        if (isPlot(i)) {
            boardGame[i]--;
            boardGame[25]++;
        }
    }

    /**
     * by convention player 1 is white
     * @return true iff this is player 1
     */
    public boolean isWhite() {
        return gameController.getWhite() == this;
    }

    /**
     * set the plate from that is currently one chipped dragged around.
     * set -1 to clear this.
     *
     * @param plate plate the chip is from.
     */
    public void setDragged(int plate) {
        draggingAround = plate;
    }

    /**
     *  get the plate with the highest index that contains at least 1 chip
     */
    public int maxPlate() {
        for (int i = 25; i >= 0; i--) {
            if (getPlate(i) > 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * adjust local plate number to (glibal) hite plate number.
     * this is only done, if 1<=no<=24, not for 0, 25
     * White doesnt need to be adjusted
     */
    public int adjustPlate(int plate) {
        if (!isWhite() && plate >= 1 && plate <= 24) {
            return 25 - plate;
        } else {
            return plate;
        }
    }

    public int[] getBoardGame() {
        return (int[])boardGame.clone();
    }

    public IntegerList getRemainingHops() {
        return (IntegerList)remainingHops.clone();
    }

}
