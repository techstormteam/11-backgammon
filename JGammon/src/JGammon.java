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



import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * The application Main-class.
 *
 * It is a singleton, only one instance exists and this can be reached from
 * everywhere by
 *    JGammon.jgammon()
 *
 * The main()-method checks for commandline arguments and creates the singleton
 * object.
 *
 * @author Aviv
 * @version 1.0
 */
public class JGammon implements ActionListener {

    public final static String VERSION = "1.0.0";
    public final static String AUTHOR = "Aviv";
    private JGamFrame jGamFrame;
    private Game game;

    private static JGammon theJGammon;

    /**
     * Construct and show the application.
     */
    private JGammon() {
        jGamFrame = new JGamFrame(this);
        centerFrame(jGamFrame);
        jGamFrame.setVisible(true);
    }

    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args) throws Exception {

        theJGammon = new JGammon();

    }
    
    /**
     * center a component on the screen
     * @param frame component to be centered
     */
    public static void centerFrame(java.awt.Component frame) {
        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);

    }

    /**
     * one of the buttons has been pressed.
     * If it is the "New game"-Button handle it here.
     * Else send a message to the game.
     * @param e ActionEvent telling which button it is
     */
    public void actionPerformed(ActionEvent e) {
        handle(e.getActionCommand());
    }

    public void handle(String command) {
        if (command.equals("newgame")) {

        	
            if (game != null) {
                if (JOptionPane.showConfirmDialog(getFrame(),
                                                  "There is a game. Abort it?",
                                                  "Start a new game",
                                                  JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) {
                    clearGame();
                } else {
                    return;
                }
            }

            Player player1 = new ComputerPlayer("Computer");
            Player player2 = new LocalPlayer("Player");
            
            try {
				game = new Game(player1, player2, this);
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            if (game != null) {
                game.start();
            }
            getFrame().repaint();

        } else if (command.equals("close")) {
            if (game != null) {
                if(JOptionPane.showConfirmDialog(getFrame(),
                                              "There is a game. Abort it?",
                                              "Quit program",
                                              JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    exit(0);
                }
            } else
                exit(0);
        } else {
            if (game != null) {
                game.handle(command);
            }
        }

    }

    /** things to be done at the end */
    private void exit(int i) {
        clearGame();
        System.exit(i);
    }

    public Game getGame() {
        return game;
    }

    public static JGammon jgammon() {
        return theJGammon;
    }

    public JGamFrame getFrame() {
        return jGamFrame;
    }

    public void clearGame() {
        if (game != null) {
            game.abort();
        }
        game = null;
        jGamFrame.setLabel("");
        jGamFrame.setIcon(null);
        jGamFrame.repaint();

    }

}
