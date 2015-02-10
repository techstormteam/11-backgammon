
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This subclass of Player is used for player at this local terminal.
 *
 * It reacts to local UI-input such as moves and button pressing
 * (ROLL; DOUBLE; GIVEUP)
 *
 * @author Aviv
 */
public class ComputerPlayer extends Player {

    /** used for commication betw. threads */
    private Object lastMessage;

    private MessageFormat msgFormat = new MessageFormat("");

    /** if this is true
     * moves with the mouse may be done
     */
    private boolean allowMoves;

    public ComputerPlayer(String name) {
        super(name);
    }

    /**
     * given an offer extract the name of it from the constant.
     * get the localized one!
     * @param offer int GIVE_UP_*
     * @return String describing the offer
     */
    private String getLevelName(int offer) {
        switch(offer) {
        case ORDINARY: return "ORDINARY";
        case GAMMON: return "GAMMON";
        case BACKGAMMON: return "BACKGAMMON";
        }
        throw new IllegalArgumentException(Integer.toString(offer));
    }

    /**
     * get the next Move.
     *
     * UI waiting
     * wait till i am waked up and then go for it ...
     *
     * @return Move
     * @throws UndoException 
     * @todo Implement this jgam.Player method
     */
    synchronized public Move move() throws InterruptedException, UndoException {
    	
        Move move = getBestMove();
        getGame().handle(move);
        return move;
    }

    // Get a random integer.
    private int randomInteger(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
          throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        int randomNumber =  (int)(fraction + aStart);
        return randomNumber;
    }
    
    // AI algorithm get best move for computer
    private Move getBestMove() {
    	Move decision = null;
    	List<Move> moves = getAllMovesInCurrentStep();
    	Random rand = new Random();
    	decision = moves.get(randomInteger(0, moves.size() - 1, rand));
    	return decision;
    }
    
    private List<Move> getAllMovesInCurrentStep() {
    	List<Move> moves = new ArrayList<Move>();
    	for (int startJag = 0; startJag <= 25; startJag++) {
    		int jag = adjustJag(startJag);
	    	List possibleMoves = getPossibleMovesFrom(jag);
	        Collections.sort(possibleMoves);
	        if (possibleMoves != null) {
	            for (Iterator iter = possibleMoves.iterator(); iter.hasNext(); ) {
	                Move move = (Move) iter.next();
	                moves.add(move);
	            }
	        }
    	}
    	return moves;
    }
    
    /**
     * a message is passed from the awtthread.
     *
     * store it and wake up a possibly waiting thread.
     *
     * @param msg Message-Object
     * @todo Implement this jgam.Player method
     */
    synchronized public void handle(Object msg) {
        lastMessage = msg;
        notify();
    }


    /**
     *
     * @return true if so
     * @todo Implement this jgam.Player method
     */
    public boolean isRemote() {
        return false;
    }

    private String[] giveups = {getLevelName(ORDINARY), getLevelName(GAMMON), getLevelName(BACKGAMMON)};
    /**
     * if this player wants to doube or give up before his/her/move.
     *
     * wait for an action!
     *
     * @param rollOnly if this is true, only ROLL is allowed
     * @return one of ROLL, DOUBLE, GIVE_UP_
     * @throws UndoException *
     * @todo Implement this jgam.Player method
     */
    synchronized public int nextStep(boolean rollOnly) throws
            InterruptedException, UndoException {

        int ret = -1;
        getGame().getJGam().getFrame().enableButtons();

        while (ret == -1) {
            wait();
            if (lastMessage.equals("roll")) {
                ret = ROLL;
                getGame().getJGam().getFrame().disableUndoButton();
            } else if (lastMessage.equals("undo")) {
                throw new UndoException(true);
            }
        }

        getGame().getJGam().getFrame().disableButtons();
        return ret;

    }


    /**
     * are UI-moves to be made right now?
     * @return true if yes
     */
    synchronized public boolean isWaitingForUIMove() {
        return allowMoves;
    }

    /** nothing to be done when aborting */
    public void abort() {}

    public void informAccept(boolean answer) {}

    public void informMove(SingleMove move) {}

    public void informRoll() {}

    /**
     * show the animation for this move.
     *
     * @param m Move to animate
     */
    public void animateMove(Move m) {
        BoardAnimation anim = new BoardAnimation(m.player(), m.from(), m.to());
        anim.animate(getGame().getBoard());
    }


}
