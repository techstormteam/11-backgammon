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


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * A BoardSnapshot describes a snapshot of a backgammon board.
 *
 * Such a snapshot can be saved to disk.
 * It can be also loaded to continue a game.
 * It can be transmitted and received on a single line.
 *
 * transmitted data is:
 *    0-23 jags:  [i] > 0 ==> white[i+1] = [i]
 *                [i] < 0 ==> blue[24-i] = [i]
 *    24 : white[25]
 *    25 : blue[25]
 *    26 : doubleCube   >= 1 white may double
 *                      <= 1 blue may double
 *    27 : white's turn?
 *    28 : the dice  (0-35)
 *
 * File Format:
 *
 * #.... are ignored
 * empty lines are ignored
 *
 * 1. white board
 * 2. blue board
 * 3. whose turn line
 * 4. double cube
 * 5. dice line
 *
 *
 * The following data is stored locally but neither transmitted over the
 * connection nor saved to / read from a file
 *  History
 * @todo transmit history over the connection and save/load in files
 *
 * @author Aviv
 * @version 1.0
 */
public class BoardSnapshot {

    public final static String NEWGAME = "NEWGAME";
    public final static String SETUPBOARD = "SETUPBOARD";

    // stores all the data!
    private int[] whiteBoard = new int[26];
    private int[] blueBoard = new int[26];

    // the comment for this board. Can be read from the stream or set
    // by setComment
    private String comment;

    private boolean whitesTurn;
    private int[] dice;

    private List history;

    /**
     * read a snapshot saved to a file.
     * @param f File to be read
     * @throws IOException file not found or read error
     */
    public BoardSnapshot(File f) throws IOException, FormatException {
        this(new FileReader(f));
    }

    /**
     * read a snapshot from a Reader
     */
    public BoardSnapshot(Reader r) throws IOException, FormatException {

        try {
            String header = readLine(r);
            if (!new String(header).equals("JGAM")) {
                throw new FormatException("Expected JGAM Header; received: " +
                                          header);
            }

            comment = readLine(r);
            String whiteLine = readLine(r);
            String blueLine = readLine(r);
            String turnLine = readLine(r);
            String cubeLine = readLine(r);
            String diceLine = readLine(r);

            parseLine(whiteBoard, whiteLine);
            parseLine(blueBoard, blueLine);
            whitesTurn = turnLine.equalsIgnoreCase("white");

            String d[] = cubeLine.split(" ");

            d = diceLine.split(" ");
            if (d.length == 2) {
                dice = new int[2];
                dice[0] = Integer.parseInt(d[0]);
                dice[1] = Integer.parseInt(d[1]);
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FormatException(ex);
        }
    }

    /**
     * parse a line that describes a board:
     * read in the jags 1-24 and the bar (25), separated by ":"
     * the chips already played off are then calculated
     *
     * @param board int[] array to write to
     * @param line read line
     */
    private void parseLine(int[] board, String line) {
        String elems[] = line.split(":");
        assert elems.length == 25;
        int total = 0;
        for (int i = 0; i < elems.length; i++) {
            board[i + 1] = Integer.parseInt(elems[i]);
            total += board[i + 1];
        }
        board[0] = 15 - total;
    }

    /**
     * take a snapshot from a game
     * @param game Game to snapshoot
     */
    public BoardSnapshot(Game game) {
        whiteBoard = game.getPlayerWhite().getBoard();
        blueBoard = game.getPlayerBlack().getBoard();
        whitesTurn = (game.getCurrentPlayer() == game.getPlayerWhite());
        dice = game.getDice();
        history = new LinkedList(game.getHistory());
    }

    

    /**
     * ignore comments (#....)
     * return null at end of stream
     * @param r Reader
     * @return String
     * @throws IOException
     */
    public static String readLine(Reader r) throws IOException {
        while (true) {
            StringBuffer ret = new StringBuffer();
            int c = r.read();
            while (c != -1 && c != '\n') {
                if (c != '\r') {
                    ret.append((char) c);
                }
                c = r.read();
            }
            if (c == -1 && ret.length() == 0) {
                return null;
            }

            String s = ret.toString();
            if (!s.startsWith("#") && s.trim().length() > 0) {
                return s;
            }
        }
    }

    public Player getCurrentPlayer(Player white, Player blue) {
        return whitesTurn ? white : blue;
    }

    public int[] getWhiteBoard() {
        return whiteBoard;
    }

    public int[] getBlueBoard() {
        return blueBoard;
    }

    public List getHistory() {
        return history;
    }

    public int[] getDice() {
        return dice;
    }

    

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Indicates whether this snapshot is equal to a different one.
     *
     * Snapshots are equal if:
     * - boards are equally setup.
     * - player in turn are equal
     * - dice are equal
     * - doubleDice are equal
     *
     * @param obj the snapshot object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *   argument; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        BoardSnapshot other = (BoardSnapshot)obj;
        return (intArrayEqual(this.getBlueBoard(), other.getBlueBoard()) &&
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
