/*
 JGammon: A Backgammon client with nice graphics written in Java
 Copyright (C) 2005 Mattias Ulbrich

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.game.backgammon;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Random;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.game.backgammon.util.AsynchronousWaitingWindow;

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
 * @author Mattias Ulbrich
 */
public class Game implements Runnable {

    private Player player1, player2;
    private Player currentPlayer;
    private JGammon jgam;

    private Random random;

    // the game runs in its own thread
    private Thread gameThread;

    // the player that may undo
    private Player undoPlayer;

    private int doubleDice = 1;
    private Player doublePlayer;

    private Player winner = null;
    public static final int SIMPLE_WIN = Player.ORDINARY;
    public static final int GAMMON_WIN = Player.GAMMON;
    public static final int BACKGAMMON_WIN = Player.BACKGAMMON;
    int winType = SIMPLE_WIN; // 1 simple, 2 gammon, 3 backgammon

    private int dice[];

    private ResourceBundle msg = ResourceBundle.getBundle("com.game.backgammon.msg.Game");
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

    public Player getPlayerBlue() {
        return player2;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOtherPlayer() {
        return (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * get a local player that is NOT remote
     * @return Player that is local
     */
    public Player getLocalPlayer() {
        if (player1.isRemote()) {
            return player2;
        } else {
            return player1;
        }
    }

    public Player getOtherPlayer(Player p) {
        return p == player1 ? player2 : player1;
    }

    public Player getUndoPlayer() {
	return undoPlayer;
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

    private void chooseBeginner() throws IOException {
        jgam.getFrame().setLabel(msg.getString("choosing"));
        jgam.getFrame().setIcon(null);
        int d[];
        do {
            d = rollDice(2);
            player1.setDice(d[0]);
            player2.setDice(d[1]);
        } while (d[0] == d[1]);

        if (d[0] > d[1]) {
            currentPlayer = player1;
        } else {
            currentPlayer = player2;
        }

        // we set the dice
        // but we show the original result!
        dice = d;
        currentPlayer.setPossibleHops(dice);
	undoPlayer = currentPlayer;

    }

    private void play() throws Exception {

        jgam.getFrame().repaint();

        if (dice != null) {
            //
            // make moves
            //

            while (currentPlayer.canMove()) {
                Move move = currentPlayer.move();
                for (Iterator iter = move.getSingleMoves().iterator();
                                     iter.hasNext(); ) {
                    SingleMove sm = (SingleMove) iter.next();
                    currentPlayer.performMove(sm);
                    getOtherPlayer().informMove(sm);
                }
                jgam.getFrame().repaint();
            }

            if (currentPlayer.hasWon()) {
                winner = currentPlayer;
                if (getOtherPlayer().getOff() == 0) {
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

            switchPlayers();
            dice = null;
        }

        // dice == null now

        //
        // GIVE_UP, ROLL
        //
        int step = currentPlayer.nextStep();
        getOtherPlayer().setDice(null);
        jgam.getFrame().repaint();
        while (step != Player.ROLL) {
            boolean answer;
            jgam.getFrame().setLabel(msgFormat.format(msg.getString(
                    "wait"),
                    new Object[] {getOtherPlayer().getName()}));
            jgam.getFrame().setIcon(AsynchronousWaitingWindow.clock);
            answer = getOtherPlayer().acceptsOffer(step);
            setCurrentPlayerLabel();
            currentPlayer.informAccept(answer);
            if (step == Player.DOUBLE) {
                if (answer) {
                    doubleDice *= 2;
                    doublePlayer = getOtherPlayer();
                    jgam.getFrame().repaint();
                } else {
                    winner = currentPlayer;
                    return;
                }
            } else if (step != Player.DOUBLE && answer) { // GIVE_UP_*
                winType = step;    // GIVE_UP_GAMMON ==> GAMMON etc.
                winner = getOtherPlayer();
                return;
            }
            step = currentPlayer.nextStep();
        }
        getOtherPlayer().informRoll();
        dice = rollDice(2);
        currentPlayer.setDice(dice);

    }

    public void run() {
        try {
            chooseBeginner();

            while (winner == null) {
                setCurrentPlayerLabel();
                play();
            }

            jgam.getFrame().repaint();
            // somewon has won.
            msgFormat.applyPattern(msg.getString("wins" + winType));
            String M = msgFormat.format(new Object[] {winner.getName()});

            int value = getDoubleValue() * winType;
            msgFormat.applyPattern(msg.getString("worth"));
            M += " " + msgFormat.format(new Object[] {new Integer(value)});
            JOptionPane.showMessageDialog(jgam.getFrame(), M,
                                          msg.getString("gameover"),
                                          JOptionPane.
                                          INFORMATION_MESSAGE,
                                          winner.getChipIcon());
            jgam.clearGame();

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
        msgFormat.applyPattern(msg.getString("turn"));
        String M = msgFormat.format(new Object[] {
                                    currentPlayer.getName(),
                                    currentPlayer.getColor()});
        jgam.getFrame().setLabel(M);
        if (currentPlayer.isRemote()) {
            jgam.getFrame().setIcon(AsynchronousWaitingWindow.clock);
        } else {
            jgam.getFrame().setIcon(currentPlayer.getChipIcon());
        }
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

    public boolean mayDouble(Player player) {
        return (doublePlayer == null || player == doublePlayer)
                && doubleDice < 64;
    }

    /**
     * get the value of the double dice (1,2,4,8,...)
     * @return a power of 2;
     */
    public int getDoubleValue() {
        return doubleDice;
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


}
