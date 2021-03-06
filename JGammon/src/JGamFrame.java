
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This is the MainWindow-class.
 *
 * On top it has got several Buttons and a message area.
 * In the center lies the board (a JComponent) and in the bottom
 * there is the chat area and the turn and flip buttons.
 *
 * Buttons are partly handled by JGam and partly by this class.
 *
 * @author Aviv
 */
public class JGamFrame extends JFrame {
	
    private JPanel contentPane;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel jToolBar = new JPanel();
    private JButton buttonNew = new JButton();
    private JButton buttonRoll = new JButton();
    private JButton buttonUndo = new JButton();
    private JLabel label = new JLabel();
    private Board board;
    private Color bgColor = new Color(199,199,207);
    private JGammon jGam;
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private Component component3 = Box.createHorizontalStrut(8);
    private JPanel bottomPanel = new JPanel();
    
    private GridBagLayout gridBagLayout2 = new GridBagLayout();

    public JGamFrame(JGammon jgam) {
        jGam = jgam;
        board = new Board(jgam);
        try {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            jbInit();
            disableButtons();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Component initialization.
     *
     * @throws java.lang.Exception
     */
    private void jbInit() throws Exception {
        contentPane = (JPanel) getContentPane();
        contentPane.setLayout(borderLayout1);
        setTitle("Backgammon - " + JGammon.AUTHOR);
        this.addWindowListener(new JGamFrame_this_windowAdapter(this));
        // Init new button
        buttonNew.setActionCommand("newgame");
        buttonNew.setText("New Game");
        // Init roll button
        buttonRoll.setActionCommand("roll");
        buttonRoll.setText("Finish");
        // Init undo button
        buttonUndo.setEnabled(true);
        buttonUndo.setActionCommand("undo");
        buttonUndo.setText("Undo");
        
        // Add click events
        buttonNew.addActionListener(jGam);
        buttonRoll.addActionListener(jGam);
        buttonUndo.addActionListener(jGam);
        
        // Create layout and GUI
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText("Welcome to" + " Backgammon " + JGammon.VERSION + " - " +  JGammon.AUTHOR);
        jToolBar.setBackground(bgColor);
        jToolBar.setLayout(gridBagLayout1);
        bottomPanel.setLayout(gridBagLayout2);
        contentPane.add(board, java.awt.BorderLayout.CENTER);
        contentPane.add(bottomPanel, java.awt.BorderLayout.SOUTH);
        contentPane.add(jToolBar, java.awt.BorderLayout.NORTH);
        jToolBar.add(component3, new GridBagConstraints(3, 1, 1, 2, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(label, new GridBagConstraints(3, 0, 1, 2, 1.0, 1.0
                , GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(buttonUndo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(buttonRoll, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(buttonNew, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        
    }

    public void setLabel(String string) {
        label.setText(string);
    }

    public void setIcon(ImageIcon icon) {
        label.setIcon(icon);
    }

    public Board getBoard() {
        return board;
    }

    public void enableButtons() {
    	buttonRoll.setEnabled(true);
    }
    
    public void enableUndoButton() {
        buttonUndo.setEnabled(true);
    }
    
    public void disableUndoButton() {
        buttonUndo.setEnabled(false);
    }

    public void disableButtons() {
       buttonRoll.setEnabled(false);
       buttonUndo.setEnabled(false);
    }

    public void closed() {
        jGam.handle("close");
    }

}

// Catch form close event. 
class JGamFrame_this_windowAdapter extends WindowAdapter {
    private JGamFrame adaptee;
    JGamFrame_this_windowAdapter(JGamFrame adaptee) {
        this.adaptee = adaptee;
    }

    public void windowClosing(WindowEvent e) {
        adaptee.closed();
    }
}

