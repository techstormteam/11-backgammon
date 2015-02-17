

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

/*
 * This is used in Board to react to mouse events, such as drag and drop and
 * double click.
 *
 * During Board.paint() this paint method is also called.
 *
 * @author Aviv
 */
public class GameMouseListener implements MouseListener, MouseMotionListener {

    private GuiOfBoard guiBoard;
    private ApplicationBackgammon backgammonApp;

    // if dragging around, what chip at what position
    private ImageIcon imageIcon;
    private Point dragLocation;

    // store the possible goals, they are painted in green
    private List MovableList;

    // where dragging began
    private int startPlate;

    // the position of the plates in the board and their sizes
    static Rectangle[] plates = new Rectangle[25];
    static Dimension outwindow = new Dimension(GuiOfBoard.PLATES_WIDTH, GuiOfBoard.OUTHEIGHT);
    static Dimension platewindow = new Dimension(GuiOfBoard.PLATES_WIDTH, GuiOfBoard.PLATES_HEIGHT);


    public GameMouseListener(GuiOfBoard b, ApplicationBackgammon g) {
        guiBoard = b;
        backgammonApp = g;
        b.addMouseListener(this);
        b.addMouseMotionListener(this);
    }

    public void mouseDragged(MouseEvent e) {
        dragLocation = e.getPoint();
        guiBoard.repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    /*
     * make the move if there is only one possible goal
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e) {

        if (e.getClickCount() != 2) {
            return;
        }

        dragLocation = e.getPoint();
        int plate = calculateWhitePlate(e.getPoint());
        Player player = null;
        if (backgammonApp.getGameController() != null) {
            player = backgammonApp.getGameController().getCurPlayer();
        }
        if (player == null || !player.WaitingForUIMove() || plate == -1) {
            return;
        }

        player.setDragged( -1);

        plate = player.adjustPlate(plate);
        MovableList = player.getPossibleMovesFrom(plate);

        if(MovableList.size() < 1)
            return;

        if(MovableList.size() == 1) {
            backgammonApp.getGameController().process(MovableList.get(0));
            return;
        }

        // check that it is clear what is meant
        // first possibility: only a single length left (doublet or only one dice left)
        if (player.getRemainingHops().distinctValues().length() == 1) {
            // the first is the one with one hop
            backgammonApp.getGameController().process(MovableList.get(0));
            return;
        }

        // 2nd poss: all moves have the same goal
        int to = ((Move) MovableList.get(0)).toPlate();
        for (Iterator iter = MovableList.iterator(); iter.hasNext(); ) {
            Move item = (Move) iter.next();
            if (item.toPlate() != to) {
                return;
            }
        }

        backgammonApp.getGameController().process(MovableList.get(0));

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /*
     * dragging begins
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e) {
        dragLocation = e.getPoint();

        Player player = null;
        if (backgammonApp.getGameController() != null) {
            player = backgammonApp.getGameController().getCurPlayer();
        }
        if (player == null || !player.WaitingForUIMove()) {
            return;
        }

        startPlate = calculateWhitePlate(e.getPoint());

        if (startPlate == -1) {
            return;
        }

        startPlate = player.adjustPlate(startPlate);

        if (player.getPlate(startPlate) <= 0) {
            startPlate = -1;
            return;
        }

        player.setDragged(startPlate);

        imageIcon = player.getChipIcon();

        MovableList = player.getPossibleMovesFrom(startPlate);

        guiBoard.repaint();
    }

    public void paintMoving(Graphics g) {

        if (imageIcon != null) {

            for (Iterator iter = MovableList.iterator(); iter.hasNext(); ) {
                Move m = (Move) iter.next();
                int jag = m.player().adjustPlate(m.toPlate());

                if (jag != 0) {
                    
                    Player p = backgammonApp.getGameController().getCurPlayer();
                    guiBoard.paintChip(imageIcon.getImage(), g, jag,
                                    p.getPlateWithDragging(p.adjustPlate(jag)));
                }
            }

            g.drawImage(imageIcon.getImage(), dragLocation.x - GuiOfBoard.PLATES_WIDTH / 2,
                        dragLocation.y - GuiOfBoard.PLATES_WIDTH / 2, null);
        }
    }

   
    
    /*
     * dragging ends
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e) {
        imageIcon = null;
        dragLocation = e.getPoint();
        Player player = null;
        if (backgammonApp.getGameController() != null) {
            player = backgammonApp.getGameController().getCurPlayer();
        }
        if (player == null || !player.WaitingForUIMove() || startPlate == -1) {
            return;
        }

        int endJag = calculateWhitePlate(e.getPoint());
        player.setDragged( -1);

        if (endJag == -1) {
            guiBoard.repaint();
            return;
        }

        // choose the shortest move from startTag to endTag
        Collections.sort(MovableList);
        if (MovableList != null) {
            endJag = player.adjustPlate(endJag);
            for (Iterator iter = MovableList.iterator(); iter.hasNext(); ) {
                Move move = (Move) iter.next();
                if (move.toPlate() == endJag) {
                    backgammonApp.getGameController().process(move);
                    backgammonApp.getGameController().getApp().getAppFrame().enableUndoButton();
                    break;
                }
            }
        }

        guiBoard.repaint();
    }


    /*
     * calculate the native plate number of a point. Native plates are
     * 0 1 2 3 .. 11
     * 12 ... 23
     * @param point Point within the board
     * @return int 0-23 a native plate number
     */
    private int calculatePlate(Point point) {
        for (int i = 0; i < plates.length; i++) {
            if (plates[i].contains(point)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * calculate the plate  number of a point. This is the correct number
     * for the white player. the blue is then 25-X.
     * these are
     * 1 2 ... 12
     * 24 23 ... 13
     * @param point Point
     * @return int 1-24
     */
    private int calculateWhitePlate(Point point) {
        int nat = calculatePlate(point);
        if (nat == -1) {
            if (getOutRectangle().contains(point)) {
                return 0; // out
            }
            return -1;
        }

        // other direction downstairs!
        if (nat >= 12 && nat <= 23) {
            nat = 35 - nat;
        }
        return guiBoard.adjustPlate(nat + 1);
    }

    private Rectangle getOutRectangle() {
        Point point = (Point) GuiOfBoard.OUT_START.clone();
        if (guiBoard.isLeftRightFlipped()) {
            point.x = GuiOfBoard.size.width - point.x - GuiOfBoard.PLATES_WIDTH;
        }
        if (!guiBoard.isPlayerOnTop(backgammonApp.getGameController().getCurPlayer())) {
            point.y = GuiOfBoard.size.height - point.y - GuiOfBoard.OUTHEIGHT;
        }

        return new Rectangle(point, outwindow);
    }


    // calculate the rectangles of the plates.
    static {
        int y2 = GuiOfBoard.size.height - GuiOfBoard.BOARD_START.y - platewindow.height;
        // here begin the lower rectangles
        for (int i = 0; i < 24; i++) {
            plates[i] = new Rectangle(GuiOfBoard.BOARD_START, platewindow);
            plates[i].translate((i % 12) * GuiOfBoard.PLATES_WIDTH, 0);
            if (i % 12 >= 6) { // beyond the bar
                plates[i].translate(GuiOfBoard.BARWIDTH, 0);
            }
            if (i >= 12) { // lower half
                plates[i].y = y2;
            }
        }

        // the bar
        plates[24] = new Rectangle((GuiOfBoard.size.width - GuiOfBoard.BARWIDTH) / 2,
                                 GuiOfBoard.BOARD_START.y,
                                 GuiOfBoard.BARWIDTH,
                                 GuiOfBoard.size.height - 2 * GuiOfBoard.BOARD_START.y);

    }


    
    

}
