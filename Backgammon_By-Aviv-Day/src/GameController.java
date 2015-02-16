
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

/**
 * This is the game itsself - the logic etc.
 * The players are contained in here as well.
 *
 * After construction a game is started by the start()-method.
 * It then runs in its own thread.
 *
 * After finding out the beginning party, the players take turns.
 * (method play()).
 *
 * A game can be stopped via the abort-method.
 *
 * @author Aviv
 */
public class GameController implements Runnable {

    private Player computerAI, human;
    private Player currentPlayer;
    private List historyData = new ArrayList();
    private ApplicationBackgammon app;

    private Random rand;

    // the game runs in its own thread
    private Thread gameThread;

    // the last snapshot: this can be saved to disk.
    private Snapshot snapshot;

    // this is the setup to which must be returned to undo
    private Snapshot usingSnapshotUndo;

    private Player winner = null;
    public static final int SIMPLE_WIN = Player.ORDINARY;
    int winType = SIMPLE_WIN; // 1 simple, 2 gammon, 3 backgammon

    private int dices[];

    private MessageFormat msgFormat = new MessageFormat("");

    public GameController(Player ai, Player player, ApplicationBackgammon jgam) throws
            IOException {
        rand = new Random();
        computerAI = ai;
        human = player;
        computerAI.setGameController(this);
        human.setGameController(this);
        this.app = jgam;

    }

    public Player getWhite() {
        return computerAI;
    }

    public Player getBlack() {
        return human;
    }

    public Player getCurPlayer() {
        return currentPlayer;
    }

    public Player getRemainingPlayer() {
        return (currentPlayer == computerAI) ? human : computerAI;
    }

    public Player getRemainingPlayer(Player p) {
        return p == computerAI ? human : computerAI;
    }

    /**
     * announce an action to the game.
     * This called from the awt thread. The message is passed to the
     * current player.
     * @param msg the object describing the message.
     */
    public void process(Object msg) {
        getCurPlayer().handle(msg);
    }

    /**
     * start a thread and save in gameThread.
     */
    public void start() {
        assert gameThread == null;
        gameThread = new Thread(this, "Game-Thread");
        gameThread.start();
    }

    private void chooseStarter() throws  IOException {
        currentPlayer = human; // player go first

    }

    private void go() throws Exception {

        app.getAppFrame().repaint(); // after an action(computer move, player move, undo button is clicked,...), game reload

        if (dices != null) {
            //
            // make moves
            //

            while (currentPlayer.canMove()) {
                Move move = currentPlayer.move(); //current player mean player with his turn now.
                // when he take action move, game will start his move
                for (Iterator iter = move.getOneMoves().iterator();
                                     iter.hasNext(); ) {
                    OneMove sm = (OneMove) iter.next();
                    currentPlayer.performMove(sm); // this line will change data(number of tiles in plates,reduce step of move...) after moving
                    historyData.add(sm); // save history for undo action
                    getRemainingPlayer().doMove(sm); // update data for opponent((number of tiles in plates,reduce step of move...) after moving
             
                }
                app.getAppFrame().repaint();// afeter move, repaint GUI
            }
            // if we are standing here means current player can't move any more
            if (currentPlayer.hasGameWon()) { // check if current player is win
                winner = currentPlayer; 
                if (getRemainingPlayer().getOff() == 0) { // getOff Means get number of tiles  are eaten
                	//win mean no tiles are eaten and ... go 
                    if (getRemainingPlayer().maxPlate() >= 18) { 
                        winType = 3;
                    } else {
                        winType = 2;
                    }
                }
                return;
            }

            //
            // switch players
            //

            changePlayers(); // I think you will understand this
            dices = null; // let's roll the dice for new turn
            snapshot = new Snapshot(this); // for undo action
        }

        // dice == null now

        //
        // ROLL
        //
        int step = currentPlayer.stepNext(); // wait for current click finish button
        getRemainingPlayer().setDice(null);
        app.getAppFrame().repaint();
        while (step != Player.ROLL) { // user must click finish button to go outside this loop
            historyData.add(new History(currentPlayer));
            setLabelCurrentPlayer();
            step = currentPlayer.stepNext();
        }
        getRemainingPlayer().doRoll();
        dices = shakeDice(2); // roll the dice
        currentPlayer.setDice(dices); // apply the dice to current player
        usingSnapshotUndo = new Snapshot(this); // this for undo action

    }

    public void run() {
        try {
            if (snapshot == null) {
                chooseStarter();
            } else {
                useSnap(snapshot);
                usingSnapshotUndo = snapshot;
            }


            while (winner == null) { // game will continue if no winner has chosen.
                try {
                    setLabelCurrentPlayer();
                    go(); // main of game is here
                } catch (UndoClickException ex) {
                    if(!usingSnapshotUndo.equals(new Snapshot(this))) { // this block of code for undo action
                        JOptionPane.showMessageDialog(app.getAppFrame(),
                                "Undo successfully");
                        setSnapshot(usingSnapshotUndo);
                        useSnap(usingSnapshotUndo);
                        getApp().getAppFrame().disableButtons();
                    }
                }
            }

            app.getAppFrame().repaint();
            // somewon has won.
            if (winType == 1) {
            	msgFormat.applyPattern("{0} wins an ordinary game.");
            } else if (winType == 2) {
            	msgFormat.applyPattern("{0} wins a GAMMON game.");
            } else if (winType == 3) {
            	msgFormat.applyPattern("{0} wins a BACKGAMMON game.");
            }
            String M = msgFormat.format(new Object[] {winner.getName()});

            JOptionPane.showMessageDialog(app.getAppFrame(), M,
                                          "GAME OVER!!!",
                                          JOptionPane.
                                          INFORMATION_MESSAGE,
                                          winner.getChipIcon());
            app.clearBackgammon(); // create new game

        } catch (InterruptedIOException ex) {
            // this is ok.
            System.err.println(
                    "Thread has been interrupted to end this thread:");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            // this is ok.
            System.err.println(
                    "Thread has been interrupted to end this thread:");
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(getApp().getAppFrame(), ex.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            app.clearBackgammon();
        }
    }

    synchronized private void changePlayers() {
		currentPlayer = getRemainingPlayer();
        setLabelCurrentPlayer();
    }

    private void setLabelCurrentPlayer() {
        msgFormat.applyPattern("{0}''s turn ({1})");
        String M = msgFormat.format(new Object[] {
                                    currentPlayer.getName(),
                                    currentPlayer.getColorName()});
        app.getAppFrame().setLabel(M);
    }

    /**
     * to abort a game the connection must be reset and
     * the running tasked must interrupted (if waiting for input)
     */
    synchronized public void reset() { 
        gameThread.interrupt();
        try {
            gameThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        if (computerAI != null) {
            computerAI.dispose();
        }
        if (human != null) {
            human.dispose();
        }
    }

    public GuiOfBoard getBoardGUI() {
        return app.getAppFrame().getBoard();
    }

    public List getHistory() {
        return Collections.unmodifiableList(historyData);
    }

    public ApplicationBackgammon getApp() {
        return app;
    }

    /**
     * get the current dice.
     * @return an array of length 2 or null if there are no dice set at present
     */
    public int[] getDice() {
        return dices;
    }


    /**
     * roll count dice
     *
     * @param count the number of dice to throw
     * @return int[] must have length of count!!
     * @todo Implement this jgam.Player method
     */
    private int[] shakeDice(int count) throws IOException {

        int ret[] = new int[count];
        for (int i = 0; i < count; i++) {
            ret[i] = getOneDice();
        }
        return ret;
    }


    /**
     * create one dice. If the debug facility "manualdice" is enabled
     * then let the user enter the value, else use random generator;
     * @return int dice value
     */
    private int getOneDice() {
        try {
            if (Boolean.getBoolean("jgam.manualdice")) {
                return Integer.parseInt(JOptionPane.showInputDialog(app.
                        getAppFrame(),
                        "Set dice value:", "3"));
            }
        } catch (Exception ex) {}
        return rand.nextInt(6) + 1;
    }

    /**
     * save the snapshot to be set when the game begins or restarts.
     * @param snapshot BoardSnapshot
     */
    void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    synchronized public void useSnap(Snapshot snapshot) {
        computerAI.setBoard(snapshot.getWhiteBoard());
        human.setBoard(snapshot.getBlackBoard());
        currentPlayer = snapshot.getCurrentPlayer(computerAI, human);
        dices = snapshot.getDice();
        currentPlayer.setDice(dices);
	List H = snapshot.getHistory();
	if(H != null)
	    historyData = H;

    }


}
