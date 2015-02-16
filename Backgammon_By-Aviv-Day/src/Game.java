
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
public class Game implements Runnable {

    private Player player1, player2;
    private Player currentPlayer;
    private List history = new ArrayList();
    private JGammon jgam;

    private Random random;

    // the game runs in its own thread
    private Thread gameThread;

    // the last snapshot: this can be saved to disk.
    private BoardSnapshot snapshot;

    // this is the setup to which must be returned to undo
    private BoardSnapshot undoSnapshot;

    private Player winner = null;
    public static final int SIMPLE_WIN = Player.ORDINARY;
    public static final int GAMMON_WIN = Player.GAMMON;
    int winType = SIMPLE_WIN; // 1 simple, 2 gammon, 3 backgammon

    private int dice[];

    private MessageFormat msgFormat = new MessageFormat("");

    public Game(Player p1, Player p2, JGammon jgam) throws
            IOException {
        random = new Random();
        player1 = p1;
        player2 = p2;
        player1.setGame(this);
        player2.setGame(this);
        this.jgam = jgam;

    }

    public Player getPlayerWhite() {
        return player1;
    }

    public Player getPlayerBlack() {
        return player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOtherPlayer() {
        return (currentPlayer == player1) ? player2 : player1;
    }

    public Player getOtherPlayer(Player p) {
        return p == player1 ? player2 : player1;
    }

    /**
     * announce an action to the game.
     * This called from the awt thread. The message is passed to the
     * current player.
     * @param msg the object describing the message.
     */
    public void handle(Object msg) {
        getCurrentPlayer().handle(msg);
    }

    /**
     * start a thread and save in gameThread.
     */
    public void start() {
        assert gameThread == null;
        gameThread = new Thread(this, "Game-Thread");
        gameThread.start();
    }

    private void chooseBeginner() throws  IOException {
        currentPlayer = player2; // player go first

    }

    private void play() throws Exception {

        jgam.getFrame().repaint(); // after an action(computer move, player move, undo button is clicked,...), game reload

        if (dice != null) {
            //
            // make moves
            //

            while (currentPlayer.canMove()) {
                Move move = currentPlayer.move(); //current player mean player with his turn now.
                // when he take action move, game will start his move
                for (Iterator iter = move.getSingleMoves().iterator();
                                     iter.hasNext(); ) {
                    SingleMove sm = (SingleMove) iter.next();
                    currentPlayer.performMove(sm); // this line will change data(number of tiles in plates,reduce step of move...) after moving
                    history.add(sm); // save history for undo action
                    getOtherPlayer().informMove(sm); // update data for opponent((number of tiles in plates,reduce step of move...) after moving
             
                }
                jgam.getFrame().repaint();// afeter move, repaint GUI
            }
            // if we are standing here means current player can't move any more
            if (currentPlayer.hasWon()) { // check if current player is win
                winner = currentPlayer; 
                if (getOtherPlayer().getOff() == 0) { // getOff Means get number of tiles  are eaten
                	//win mean no tiles are eaten and ... go 
                    if (getOtherPlayer().maxJag() >= 18) { 
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

            switchPlayers(); // I think you will understand this
            dice = null; // let's roll the dice for new turn
            snapshot = new BoardSnapshot(this); // for undo action
        }

        // dice == null now

        //
        // ROLL
        //
        int step = currentPlayer.nextStep(); // wait for current click finish button
        getOtherPlayer().setDice(null);
        jgam.getFrame().repaint();
        while (step != Player.ROLL) { // user must click finish button to go outside this loop
            history.add(new HistoryMessage(step, currentPlayer));
            setCurrentPlayerLabel();
            step = currentPlayer.nextStep();
        }
        getOtherPlayer().informRoll();
        dice = rollDice(2); // roll the dice
        currentPlayer.setDice(dice); // apply the dice to current player
        undoSnapshot = new BoardSnapshot(this); // this for undo action

    }

    public void run() {
        try {
            if (snapshot == null) {
                chooseBeginner();
            } else {
                applySnapshot(snapshot);
                undoSnapshot = snapshot;
            }


            while (winner == null) { // game will continue if no winner has chosen.
                try {
                    setCurrentPlayerLabel();
                    play(); // main of game is here
                } catch (UndoException ex) {
                    if(!undoSnapshot.equals(new BoardSnapshot(this))) { // this block of code for undo action
                        JOptionPane.showMessageDialog(jgam.getFrame(),
                                "The last moves have been undone");
                        setSnapshot(undoSnapshot);
                        applySnapshot(undoSnapshot);
                        getJGam().getFrame().disableButtons();
                    }
                }
            }

            jgam.getFrame().repaint();
            // somewon has won.
            if (winType == 1) {
            	msgFormat.applyPattern("{0} wins an ordinary game.");
            } else if (winType == 2) {
            	msgFormat.applyPattern("{0} wins a GAMMON game.");
            } else if (winType == 3) {
            	msgFormat.applyPattern("{0} wins a BACKGAMMON game.");
            }
            String M = msgFormat.format(new Object[] {winner.getName()});

            JOptionPane.showMessageDialog(jgam.getFrame(), M,
                                          "GAME OVER",
                                          JOptionPane.
                                          INFORMATION_MESSAGE,
                                          winner.getChipIcon());
            jgam.clearGame(); // create new game

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
            JOptionPane.showMessageDialog(getJGam().getFrame(), ex.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            jgam.clearGame();
        }
    }

    synchronized private void switchPlayers() {
		currentPlayer = getOtherPlayer();
        setCurrentPlayerLabel();
    }

    private void setCurrentPlayerLabel() {
        msgFormat.applyPattern("{0}''s turn ({1})");
        String M = msgFormat.format(new Object[] {
                                    currentPlayer.getName(),
                                    currentPlayer.getColor()});
        jgam.getFrame().setLabel(M);
    }

    /**
     * to abort a game the connection must be reset and
     * the running tasked must interrupted (if waiting for input)
     */
    synchronized public void abort() { 
        gameThread.interrupt();
        try {
            gameThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        if (player1 != null) {
            player1.dispose();
        }
        if (player2 != null) {
            player2.dispose();
        }
    }

    public Board getBoard() {
        return jgam.getFrame().getBoard();
    }

    public List getHistory() {
        return Collections.unmodifiableList(history);
    }

    public JGammon getJGam() {
        return jgam;
    }

    /**
     * get the current dice.
     * @return an array of length 2 or null if there are no dice set at present
     */
    public int[] getDice() {
        return dice;
    }


    /**
     * roll count dice
     *
     * @param count the number of dice to throw
     * @return int[] must have length of count!!
     * @todo Implement this jgam.Player method
     */
    private int[] rollDice(int count) throws IOException {

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
                return Integer.parseInt(JOptionPane.showInputDialog(jgam.
                        getFrame(),
                        "Set dice value:", "3"));
            }
        } catch (Exception ex) {}
        return random.nextInt(6) + 1;
    }

    /**
     * save the snapshot to be set when the game begins or restarts.
     * @param snapshot BoardSnapshot
     */
    void setSnapshot(BoardSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    synchronized public void applySnapshot(BoardSnapshot snapshot) {
        player1.setBoard(snapshot.getWhiteBoard());
        player2.setBoard(snapshot.getBlackBoard());
        currentPlayer = snapshot.getCurrentPlayer(player1, player2);
        dice = snapshot.getDice();
        currentPlayer.setDice(dice);
	List H = snapshot.getHistory();
	if(H != null)
	    history = H;

    }


}
