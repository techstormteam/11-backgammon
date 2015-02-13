

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This subclass of Player is used for player at this local terminal.
 *
 * It reacts to local UI-input such as moves and button pressing
 * (ROLL; DOUBLE; GIVEUP)
 *
 * @author Aviv
 */
public class LocalPlayer extends Player {

    /** used for commication betw. threads */
    private Object lastMessage;

    private MessageFormat msgFormat = new MessageFormat("");

    /** if this is true
     * moves with the mouse may be done
     */
    private boolean allowMoves;

    public LocalPlayer(String name) {
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
        while (true) {
            allowMoves = true;
            wait();
            allowMoves = false;
            if (lastMessage instanceof Move) {
                Move m = (Move) lastMessage;
                return m;
            }
            if (lastMessage.equals("undo")) {
                throw new UndoException(true);
            }
        }
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
     * @return one of ROLL, DOUBLE, GIVE_UP_*
     * @todo Implement this jgam.Player method
     */
    synchronized public int nextStep(boolean rollOnly) throws
            InterruptedException {

        getGame().getJGam().getFrame().disableButtons();
        return ROLL;

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

    public void animateMove(Move m) {}


}
