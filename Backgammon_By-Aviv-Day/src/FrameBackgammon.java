
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

/*
 * This is the MainWindow-class.
 *
 * On top it has got several Buttons and a message area.
 * In the center lies the board (a JComponent).
 *
 * Buttons are partly handled by backgammonApp and partly by this class.
 *
 * @author Aviv
 */
public class FrameBackgammon extends JFrame {
	
	private GuiOfBoard guiBoard;
    private Color backgColor = new Color(199,199,207);
    private ApplicationBackgammon backgammonApp;
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private Component component3 = Box.createHorizontalStrut(8);
    private JPanel bottomPanel = new JPanel();
    private JPanel contentMainPane;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel jToolBar = new JPanel();
    private JButton btnNew = new JButton();
    private JButton btnFinish = new JButton();
    private JButton btnUndo = new JButton();
    private JLabel label = new JLabel();
    
    
    private GridBagLayout gridBagLayout2 = new GridBagLayout();

    public FrameBackgammon(ApplicationBackgammon app) {
        backgammonApp = app;
        guiBoard = new GuiOfBoard(app);
        try {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setResizable(false);
            createBackgammon();
            disableButtons();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /*
     * Component initialization.
     */
    private void createBackgammon() throws Exception {
        contentMainPane = (JPanel) getContentPane();
        contentMainPane.setLayout(borderLayout1);
        setTitle("Backgammon - by Aviv Day");
        this.addWindowListener(new BackgammonWindowAdapter(this));
        // Init new button
        btnNew.setActionCommand("newgame");
        btnNew.setText("Start A New Game!");
        // Init roll button
        btnFinish.setActionCommand("finish");
        btnFinish.setText("Finish Move!");
        // Init undo button
        btnUndo.setEnabled(true);
        btnUndo.setActionCommand("undo");
        btnUndo.setText("Undo Move");
        
        // Add click events
        btnNew.addActionListener(backgammonApp);
        btnFinish.addActionListener(backgammonApp);
        btnUndo.addActionListener(backgammonApp);
        
        // Create layout and GUI
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText("Welcome to" + " Backgammon 1.0.0 - by Aviv Day");
        jToolBar.setBackground(backgColor);
        jToolBar.setLayout(gridBagLayout1);
        bottomPanel.setLayout(gridBagLayout2);
        contentMainPane.add(guiBoard, java.awt.BorderLayout.CENTER);
        contentMainPane.add(bottomPanel, java.awt.BorderLayout.SOUTH);
        contentMainPane.add(jToolBar, java.awt.BorderLayout.NORTH);
        jToolBar.add(component3, new GridBagConstraints(3, 1, 1, 2, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(label, new GridBagConstraints(3, 0, 1, 2, 1.0, 1.0
                , GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(btnUndo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(btnFinish, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        jToolBar.add(btnNew, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        
    }

    public void setLabel(String string) {
        label.setText(string);
    }

    public GuiOfBoard getBoard() {
        return guiBoard;
    }

    public void enableButtons() {
    	btnFinish.setEnabled(true);
    }
    
    public void enableUndoButton() {
        btnUndo.setEnabled(true);
    }
    
    public void disableUndoButton() {
        btnUndo.setEnabled(false);
    }

    public void disableButtons() {
       btnFinish.setEnabled(false);
       btnUndo.setEnabled(false);
    }

    public void closed() {
        backgammonApp.process("close");
    }

}

// Catch form close event. 
class BackgammonWindowAdapter extends WindowAdapter {
    private FrameBackgammon f;
    BackgammonWindowAdapter(FrameBackgammon frame) {
        this.f = frame;
    }

    public void windowClosing(WindowEvent e) {
        f.closed();
    }
}

