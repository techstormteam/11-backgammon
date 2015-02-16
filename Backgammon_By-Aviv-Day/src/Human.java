

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This subclass of Player is used for player at this local terminal.
 *
 * It reacts to local UI-input such as moves and button pressing
 * (ROLL)
 *
 * @author Aviv
 */
public class Human extends Player {

    /** used for commication betw. threads */
    private Object lastMessage;

    /** if this is true
     * moves with the mouse may be done
     */
    private boolean allowMoves;

    public Human(String name) {
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
        while (true) {
            allowMoves = true;
            wait();
            allowMoves = false;
            if (lastMessage instanceof Move) {
                Move m = (Move) lastMessage;
                return m;
            }
            if (lastMessage.equals("undo")) {
                throw new UndoClickException(true);
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
     * if this player wants to doube or give up before his/her/move.
     *
     * wait for an action!
     *
     * @param rollOnly if this is true, only ROLL is allowed
     * @return one of ROLL
     * @todo Implement this jgam.Player method
     */
    synchronized public int stepNext(boolean rollOnly) throws
            InterruptedException {

        getGame().getApp().getAppFrame().disableButtons();
        return ROLL;

    }


    /**
     * are UI-moves to be made right now?
     * @return true if yes
     */
    synchronized public boolean WaitingForUIMove() {
        return allowMoves;
    }

    /** nothing to be done when aborting */
    public void abort() {}

    public void doAccept(boolean answer) {}

    public void doMove(OneMove move) {}

    public void doRoll() {}

    public void animateMove(Move m) {}


}
