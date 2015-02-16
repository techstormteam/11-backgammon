
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This subclass of Player is used for player at this local terminal.
 *
 * It reacts to local UI-input such as moves and button pressing
 * (ROLL)
 *
 * @author Aviv
 */
public class AI extends Player {

    /** used for commication betw. threads */
    private Object communicationMessa;

    /** if this is true
     * moves with the mouse may be done
     */
    private boolean allowMoving;

    public AI(String name) {
        super(name);
    }

    /**
     * get the next Move.
     *
     * UI waiting
     * wait till i am waked up and then go for it ...
     *
     * @return Move
     * @throws UndoClickException 
     * @todo Implement this jgam.Player method
     */
    synchronized public Move move() throws InterruptedException, UndoClickException {
    	
        Move move = getBestMove();
        getGame().process(move);
        return move;
    }

    /*Get a random integer.
    private int randomInteger(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
         throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        int randomNumber =  (int)(fraction + aStart);
        return randomNumber;
    }*/
    
    // AI algorithm get best move for computer
    private Move getBestMove() {
    	List<Move> moves = getAllMovesInCurrentStep();
    	
    	//1. AI Looks if theres any white (his) tile alone, 
    	//if there is he covers it \ move it to a safe place. 
    	//if there is more than one tile it choose the tile that closest to the computer "house". 
    	//If there is no tiles, than he make other step(next one).
    	List<Integer> jagsAlone = getJagsAlone();
    	if (jagsAlone.size() == 1) { 
    		// if there is he covers it \ move it to a safe place.
    		Move move = moveSafeOfAt(jagsAlone.get(0), moves);
    		if (move != null) {
    			return move;
    		}
    		
    	} else if (jagsAlone.size() > 1) {
    		// if there is more than one tile it choose the tile that closest to the computer "house".
    		int maxJag = -1;
    		for (Integer jag : jagsAlone) {
				if (jag > maxJag) {
					maxJag = jag;
				}
			}
    		Move move = moveSafeOfAt(maxJag, moves);
    		if (move != null) {
    			return move;
    		}
    		
    	}
    	
		// 2. if there no alone tiles: computer check if can build a "home"
		// home means: take one tile from each row and make them on same one inside the house example:
		// game just started, nothing moved yet so there is no alone tiles, but roll was 4-2.
		// there is 3 pack of tiles out side house, and 5 pack tiles inside the house.
		// Game will take 1 from the 3 pack and movie 4 steps forward and 1 from the 5 pack and move it 2 steps forward now, 
		// their on the same plate.
		// this called a home.(only when it happens inside the house.)
	
    	// 3. if he can't do a home, his moving by deafult, 
		// take from 3 pack or more to a 2 pack or more.(game doesn't leave any tile alone.)
    	
    	Map<Integer, Integer> toDuplicate = new HashMap<Integer, Integer>();
    	for (Move move : moves) {
    		if (!toDuplicate.containsKey(move.toPlate())) {
    			toDuplicate.put(move.toPlate(), 1);
    		} else {
    			toDuplicate.put(move.toPlate(), toDuplicate.get(move.toPlate()) + 1);
    		}
		}
    	
    	if (getRemainingHops().length() > 1) {
	    	for (Integer to : toDuplicate.keySet()) {
				if (toDuplicate.get(to) > 1) {
					for (Move move : moves) {
						if (to == move.toPlate()) {
							return move;
						}
					}
				}
			}
    	}
    	
		// 4. if he can't move without leaving tile alone, game look for opponent(black tile) alone. 
		// if he finds one and he can eat it
		// (if game can with the roll number can get to the exact place of the alone tile, 
		// one of them at least). he eats them and the opponent need to go back to the start.
		for (Move move : moves) {
			if (getRemainingPlayer().getBoardGame()[25 - move.toPlate()] > 0) {
				return move;
			}
		}
		
		// 5. if he can't eat it move by deafult with the farest tile that can move
		// (farest from the home.)
		Move farestTileMove = null;
		int fromMinJag = Integer.MAX_VALUE;
		for (Move move : moves) {
			if (fromMinJag > move.fromPlate()) {
				fromMinJag = move.fromPlate();
				farestTileMove = move;
			}
		}
		if (farestTileMove != null) {
			return farestTileMove;
		}
    		
    	
    	//Random rand = new Random();
    	//Move randomMove = moves.get(randomInteger(0, moves.size() - 1, rand));
    	//return randomMove;
		return null;
    }
    
    // move to safe place if tile alone
    private Move moveSafeOfAt(int jag, List<Move> moves) {
    	for (Move move : moves) {
			if (jag == move.toPlate() && getBoardGame()[move.fromPlate()] != 2) {
				return move;
			} else if (jag == move.fromPlate()) {
				return move;
			}
		}
    	return null;
    }
    
    private List<Integer> getJagsAlone() {
    	List<Integer> result = new ArrayList<Integer>();
    	for (int startJag = 0; startJag <= 25; startJag++) {
    		int jag = adjustPlate(startJag);
    		if (getBoardGame()[jag] == 1) {
    			result.add(jag);
    		}
		}
    	return result;
    }
    
    // Get all moves that computer can go
    private List<Move> getAllMovesInCurrentStep() {
    	List<Move> moves = new ArrayList<Move>();
    	for (int startJag = 0; startJag <= 25; startJag++) {
    		int jag = adjustPlate(startJag);
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
        communicationMessa = msg;
        notify();
    }


    /**
     * if this player wants to doube or give up before his/her/move.
     *
     * wait for an action!
     *
     * @param rollOnly if this is true, only ROLL is allowed
     * @return one of ROLL
     * @throws UndoClickException *
     * @todo Implement this jgam.Player method
     */
    synchronized public int stepNext(boolean rollOnly) throws
            InterruptedException, UndoClickException {

        int ret = -1;
        getGame().getApp().getAppFrame().enableButtons();

        
        while (ret == -1) {
            wait();
            if (communicationMessa.equals("roll")) {
                ret = ROLL;
                getGame().getApp().getAppFrame().disableUndoButton();
            } else if (communicationMessa.equals("undo")) {
                throw new UndoClickException(true);
            }
        }

        getGame().getApp().getAppFrame().disableButtons();
        return ret;

    }


    /**
     * are UI-moves to be made right now?
     * @return true if yes
     */
    synchronized public boolean WaitingForUIMove() {
        return allowMoving;
    }

    /** nothing to be done when aborting */
    public void abort() {}

    public void doAccept(boolean answer) {}

    public void doMove(OneMove move) {}

    public void doRoll() {}

    /**
     * show the animation for this move.
     *
     * @param m Move to animate
     */
    public void animateMove(Move m) {
        ComputerAnimation anim = new ComputerAnimation(m.player(), m.fromPlate(), m.toPlate());
        anim.doComputerAnimation(getGame().getBoardGUI());
    }


}
