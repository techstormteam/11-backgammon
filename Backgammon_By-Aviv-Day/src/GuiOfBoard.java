
import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * Component to draw the backgammon board.
 *
 * It fetches the information from a Game-instance.
 *
 * Additionally it allows drag and drop via a BoardMouseListener.
 * Additionally it allows an Animation to be drawn via BoardAnimation
 *
 * @author Aviv
 */
public class GuiOfBoard extends JComponent {

	/*
     * images
     */
    private static ImageIcon bg = new ImageIcon(GuiOfBoard.class.getResource(
            "background.png"));
    static ImageIcon whiteIcon = new ImageIcon(GuiOfBoard.class.getResource(
            "whiteChip.png"));
    static ImageIcon blackIcon = new ImageIcon(GuiOfBoard.class.getResource(
            "blackChip.png"));
    private static ImageIcon[] blackDice = new ImageIcon[6];
    private static ImageIcon[] whiteDice = new ImageIcon[6];

    static {
        for (int i = 1; i <= 6; i++) {
            blackDice[i - 1] = new ImageIcon(GuiOfBoard.class.getResource(
                    "black" + i + ".png"));
            whiteDice[i - 1] = new ImageIcon(GuiOfBoard.class.getResource(
                    "white" + i + ".png"));
        }
    }
	
	/*
     * widths
     */
    static final int PLATES_WIDTH = 48;
    static final int PLATES_HEIGHT = 270;
    static final int OUTHEIGHT = 400;
    // instead of chipdiameter often platewidth is used being the same
    static final int DIAMETER = 48;
    static final int THICKNESS = 15;
    static final int BARWIDTH = 97;
    static final int DICE_DIST = 80;
    static final Dimension size = calculateSize();

    /**
     * Points
     */
    // point where the first plate starts (upper left)
    static final Point BOARD_START = new Point(95, 24);
    // position to draw the first dice to
    static final Point DICE_POINT = new Point(150, 303);
    // the point at which the upper left out field begins
    static final Point OUT_START = new Point(24, 24);

	public void paintChip(Image image, Graphics g, int plate, int plateWithDragging) {
		// TODO Auto-generated method stub
		
	}
	
    // the appl.
    private ApplicationBackgammon app;

    // dragging is done with this
    private GameMouseListener gameMouseListener;

    // animated dragging
    private ComputerAnimation computerAnimation;

    /*
     * some drawing variants
     */
    private boolean topBottomflip = false;
    private boolean leftRightflip = true;

    public GuiOfBoard(ApplicationBackgammon gammonApp) {
    	
        this.app = gammonApp;
        gameMouseListener = new GameMouseListener(this, gammonApp);
    }

    /**
     * calculate the size of this canvas. This is the dimension of the
     * background
     * @return Dimension of this canvas
     */
    private static Dimension calculateSize() {
        return new Dimension(bg.getIconWidth(),
                             bg.getIconHeight());
    }

    public Dimension getPreferredSize() {
        return size;
    }
    
    /**
     * paint the board.
     * Player 1 white
     * Player 2 black
     * @param g Graphics
     */
    public void paint(Graphics g) {
        g.drawImage(bg.getImage(), 0, 0, null, this);
        GameController gameController = getGameController();
        if (gameController == null) {
            return;
        }

        // direction 1:
        Player p = gameController.getWhite();
        for (int index = 1; index <= 24; index++) {
            paintPlate(whiteIcon, g, index, p.getPlateWithDragging(index));
        }

        // the other
        p = gameController.getBlack();
        for (int index = 1; index <= 24; index++) {
            paintPlate(blackIcon, g, 25 - index, p.getPlateWithDragging(index));
        }

        // the dice
        // player 1
        int d[] = gameController.getWhite().getShownDice();
        if (d != null) {
            for (int i = 0; i < d.length; i++) {
                int x = DICE_POINT.x + DICE_DIST * i;
                g.drawImage(whiteDice[d[i] - 1].getImage(), x,
                            DICE_POINT.y, this);
            }
        }

        // player 2
        d = gameController.getBlack().getShownDice();
        if (d != null) {
            for (int i = 0; i < d.length; i++) {
                int x = size.width - (DICE_POINT.x + DICE_DIST * i) - 49;
                g.drawImage(blackDice[d[i] - 1].getImage(), x,
                            DICE_POINT.y, this);
            }
        }

        // the bar
        if(topBottomflip) {
            paintPlate(whiteIcon, g, 0, gameController.getWhite().getPlateWithDragging(25));
            paintPlate(blackIcon, g, 25, gameController.getBlack().getPlateWithDragging(25));
        } else {
            paintPlate(whiteIcon, g, 25, gameController.getWhite().getPlateWithDragging(25));
            paintPlate(blackIcon, g, 0, gameController.getBlack().getPlateWithDragging(25));
        }

        // dragged objects
       gameMouseListener.paintMoving(g);

        // animation
        if (computerAnimation != null) {
            computerAnimation.paint(g);
        }
    }

    /**
     * get the lower left corner of the outfield for a player
     * @param player Player
     * @return Point
     */
    public Point getOutPoint(Player player) {
        int ypos = 0;
        if (isPlayerOnTop(player)) {
            ypos = OUT_START.y + OUTHEIGHT;
        } else {
            ypos = size.height - OUT_START.y;
        }

        int xpos = 0;
        if (isLeftRightFlipped()) {
            xpos = size.width - OUT_START.x - DIAMETER;
        } else {
            xpos = OUT_START.x;
        }

        return new Point(xpos, ypos);
    }

    /**
     * transform a logical plate number to one on the screen.
     * this depends also
     * on the color.
     * Flipping plays into it as well
     * @param number int to be corrected
     */
    public int adjustPlate(int number) {
        if (number == 0 || number == 25) {
            return number;
        }

        if (topBottomflip) {
            number = 25 - number;
        }
        if (leftRightflip) {
            if (number <= 12) {
                number = 13 - number;
            } else {
                number = 37 - number;
            }
        }
        return number;
    }

    /**
     * paint a plate with chips from a certain color.
     * plates are corrected internally!
     *
     * @param chip ImageIcon that holds the chip
     * @param plate number of the plate (1-24)
     * @param count number of chips to paint
     */
    public void paintPlate(ImageIcon chip, Graphics g, int plate, int count) {
        for (int i = 0; i < count; i++) {
            paintChip(chip, g, plate, i);
        }
    }

    public void paintChip(ImageIcon chip, Graphics g, int plate, int number) {

        Point p = getPointForChip(plate, number);
        g.drawImage(chip.getImage(), p.x, p.y, null, this);

    }

    /**
     * get the point to draw the chip at for a specific position an a plate.
     * plate 1-24 are as seen from white. 0 is lower bar, 25 is upper bar.
     *
     * @param plate int
     * @param number int
     * @return Point
     */
    public Point getPointForChip(int plate, int number) {

        Point p;

        //  bar first!   25 is bar   0 is adjusted bar (25-25)
        if (plate == 25) {
            return new Point((size.width - PLATES_WIDTH) / 2,
                             (2 + number) * PLATES_WIDTH);
        }

        if (plate == 0) {
            return new Point((size.width - PLATES_WIDTH) / 2,
                             size.height - ((3 + number) * PLATES_WIDTH));
        }

        plate = adjustPlate(plate);

        int offset = (number % 9) * PLATES_WIDTH;
        if (number >= 5 && number <= 8) {
            offset = (number - 5) * PLATES_WIDTH + PLATES_WIDTH / 2;
        }

        if (plate <= 12) {
            p = (Point) BOARD_START.clone();
            p.translate(PLATES_WIDTH * (plate - 1), offset);
            // beyond the bar
            if (plate >= 7) {
                p.translate(BARWIDTH, 0);
            }
        } else {
            p = new Point(size.width - BOARD_START.x,
                          size.height - BOARD_START.y);
            p.translate( -PLATES_WIDTH * (plate - 12), -PLATES_WIDTH - offset);
            // beyond the bar
            if (plate >= 19) {
                p.translate( -BARWIDTH, 0);
            }
        }

        return p;
    }

    public GameController getGameController() {
        if (app != null) {
            return app.getGameController();
        } else {
            return null;
        }
    }

    public void setComputerAnimation(ComputerAnimation animation) {
        this.computerAnimation = animation;
    }


    public boolean isLeftRightFlipped() {
        return leftRightflip;
    }

    public boolean isTopBottomFlipped() {
        return topBottomflip;
    }

    /**
     * return true if the player has his home in the upper half of the board.
     * this is the case if not vert-flipped and white or flipped and black
     * @param player Player
     * @return boolean
     */
    public boolean isPlayerOnTop(Player player) {
        return player.isWhite() != isTopBottomFlipped();
    }


}
