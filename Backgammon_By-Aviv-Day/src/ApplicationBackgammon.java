

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
public class ApplicationBackgammon implements ActionListener {

    public final static String VERSION = "1.0.0";
    public final static String AUTHOR = "Aviv";
    private FrameBackgammon backgammonFrame;
    private GameController gameController;

    private static ApplicationBackgammon backgammonApp;

    /**
     * Game Properties
     */
    private ApplicationBackgammon() {
        backgammonFrame = new FrameBackgammon(this);
        alignFrameCenter(backgammonFrame);
        backgammonFrame.setVisible(true);
    }

    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args) throws Exception {

        backgammonApp = new ApplicationBackgammon();

    }
    
    /**
     * center a component on the screen
     * @param frame component to be centered
     */
    public static void alignFrameCenter(java.awt.Component frame) {
        // Center the window
        Dimension gammonScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension gammonframeSize = frame.getSize();
        if (gammonframeSize.height > gammonScreenSize.height) {
            gammonframeSize.height = gammonScreenSize.height;
        }
        if (gammonframeSize.width > gammonScreenSize.width) {
            gammonframeSize.width = gammonScreenSize.width;
        }
        frame.setLocation((gammonScreenSize.width - gammonframeSize.width) / 2,
                          (gammonScreenSize.height - gammonframeSize.height) / 2);

    }

    /**
     * one of the buttons has been pressed.
     * If it is the "New game"-Button handle it here.
     * Else send a message to the game.
     * @param e ActionEvent telling which button it is
     */
    public void actionPerformed(ActionEvent e) {
        process(e.getActionCommand());
    }

    public void process(String action) {
        if (action.equals("newgame")) {

        	
            if (gameController != null) {
                if (JOptionPane.showConfirmDialog(getAppFrame(),
                                                  "Are You Sure You Want To Quit?",
                                                  "Start a new game",
                                                  JOptionPane.YES_NO_OPTION)
                    == JOptionPane.YES_OPTION) {
                    clearBackgammon();
                } else {
                    return;
                }
            }

            // Init computer and player
            Player ai = new AI("Computer");
            Player human = new Human("Player");
            
            try {
				gameController = new GameController(ai, human, this);
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            if (gameController != null) {
                gameController.start();
            }
            getAppFrame().repaint();

        } else if (action.equals("close")) { // execute when close button clicked
            if (gameController != null) {
                if(JOptionPane.showConfirmDialog(getAppFrame(),
                                              "Are You Sure You Want To Quit?",
                                              "Quit program",
                                              JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    quit(0);
                }
            } else
                quit(0);
        } else {
            if (gameController != null) {
                gameController.process(action); // send event to current player to process
            }
        }

    }

    public static ApplicationBackgammon getApp() {
        return backgammonApp;
    }

    public FrameBackgammon getAppFrame() {
        return backgammonFrame;
    }
    
    /** things to be done at the end */
    private void quit(int i) {
        clearBackgammon();
        System.exit(i);
    }

    public GameController getGameController() {
        return gameController;
    }
    

    // repaint game.
    public void clearBackgammon() {
        if (gameController != null) {
            gameController.reset();
        }
        gameController = null;
        backgammonFrame.setLabel("");
        backgammonFrame.repaint();

    }

}
