

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
     * Game Properties
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
                                                  "Are You Sure You Want To Quit?",
                                                  "Start a new game",
                                                  JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) {
                    clearGame();
                } else {
                    return;
                }
            }

            // Init computer and player
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

        } else if (command.equals("close")) { // execute when close button clicked
            if (game != null) {
                if(JOptionPane.showConfirmDialog(getFrame(),
                                              "Are You Sure You Want To Quit?",
                                              "Quit program",
                                              JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    exit(0);
                }
            } else
                exit(0);
        } else {
            if (game != null) {
                game.handle(command); // send event to current player to process
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

    // repaint game.
    public void clearGame() {
        if (game != null) {
            game.abort();
        }
        game = null;
        jGamFrame.setLabel("");
        jGamFrame.repaint();

    }

}
