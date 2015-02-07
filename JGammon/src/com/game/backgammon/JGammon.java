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
public class JGammon implements ActionListener, ConnectionListener {

    public final static String VERSION = "1.0.0";
    public final static String AUTHOR = "Aviv";
    private JGamFrame jGamFrame;
    private NewGameDialog newGameDialog;
    private Game game;

    private static JGammon theJGammon;

    /**
     * Construct and show the application.
     */
    private JGammon() {
        jGamFrame = new JGamFrame(this);
        newGameDialog = new NewGameDialog(this);
        centerFrame(jGamFrame);
        jGamFrame.setVisible(true);
    }

    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args) throws Exception {

        try {
            System.getProperties().load(new FileInputStream("jgam.properties"));
        } catch (IOException ex) {} // egal

        String mode = null;
        String port = null;
        String server = null;
        String name1 = null;
        String name2 = null;
        String board = null;
        int cur = 0;
        try {
            for (cur = 0; cur < args.length; cur++) {
                if (args[cur].equalsIgnoreCase("-serverWhite")) {
                    mode = "serverWhite";
                    port = args[++cur];
                } else if (args[cur].equalsIgnoreCase("-serverBlue")) {
                    mode = "serverBlue";
                    port = args[++cur];
                } else if (args[cur].equalsIgnoreCase("-client")) {
                    mode = "client";
                    String S[] = args[++cur].split(":");
                    server = S[0];
                    port = S[1];
                } else if (args[cur].equalsIgnoreCase("-gnubg")) {
                    mode = "gnubg";
                    String S[] = args[++cur].split(":");
                    server = S[0];
                    port = S[1];
                } else if (args[cur].equalsIgnoreCase("-local")) {
                    mode = "local";
                } else if (args[cur].equalsIgnoreCase("-name")) {
                    if (name1 != null) {
                        name2 = args[++cur];
                    } else {
                        name1 = args[++cur];
                    }
                } else if (args[cur].equalsIgnoreCase("-board")) {
                    board = args[++cur];
                } else if (args[cur].equalsIgnoreCase("-help")) {
                    help();
                    System.exit(0);
                } else if (args[cur].equalsIgnoreCase("-hint")) {
                    HintFrame.showHintFrame();
                } else
                {
                    System.err.println("Unknown commandline option: " +
                                       args[cur]);
                    System.err.println("Try \"-help\"");
                    System.exit(0);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            System.err.println("All options need an argument! Try -help");
            System.exit(0);
        }

        theJGammon = new JGammon();

        if (mode != null) {
            theJGammon.newGameDialog.feed(mode, port, server, name1, name2, board);
            theJGammon.handle("newgame");
        }

    }

    private static void help() {
        System.out.println("Command line parameters:\n");
        System.out.println(
                "  -client <server>:<port>         starts with connecting to a server");
        System.out.println(
                "  -serverWhite <port>             starts as server, local player is white");
        System.out.println(
                "  -serverBlue <port>              starts as server, local player is blue");
        System.out.println(
                "  -local                          starts a local game");
        System.out.println(
                "  -gnubg <server>:<port>          starts with connecting to a gnubg-server");
        System.out.println("  -name                           provides a name for a player (2 can be specified)");
        System.out.println(
                "  -board <file>                   sets the board file to be loaded");
        System.out.println(
                "\nAfter the main window is launched, all you got to do is press OK\n");
    }

    /**
     * return the VERSION plus some information about enabled features
     * @return String extended Version
     */
    public static String getExtVersion() {
        String ret = VERSION;
        if (Boolean.getBoolean("jgam.manualdice")) {
            ret += " MD";
        }
        if (System.getProperty("jgam.initialboard") != null) {
            ret += " modified-initial-board";
        }
        if (Boolean.getBoolean("jgam.unsecuredice")) {
            ret += " UD";
        }
        return ret;
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

            game = newGameDialog.showAndEval();

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

    public void handleConnectionMessage(ConnectionMessage cm) {
        if (cm.getType() == cm.CLOSED) {
            JOptionPane.showMessageDialog(getFrame(),
                                          "The network connection has been lost, but you can save the board",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            clearGame();
        }
    }


    private FileFilter boardFileFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return (pathname.getName().toLowerCase().endsWith(".board")
                    || pathname.isDirectory());
        }

        public String getDescription() {
            return "Saved backgammon boards (*.board)";
        }
    };


}
