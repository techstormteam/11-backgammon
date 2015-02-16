
import javax.swing.*;
import java.awt.*;

/**
 * Animation that is shown if the remote player drags a chip.
 *
 * It is one in a thread of its own.
 *
 * @author Aviv
 * @version 1.0
 */
public class ComputerAnimation extends Thread {

    private Player currentPlayer;
    private ImageIcon imageIcon;
    private int fromPlate;
    private int toPlate;

    private double currentX, currentY;
    private double offsetX, offsetY;

    

    

    public ComputerAnimation(Player player, int from, int to) {
        this.currentPlayer = player;
        this.imageIcon = player.getChipIcon();

        fromPlate = from;
        toPlate = to;
    }

 // Draw chip icon
    synchronized public void paint(Graphics g) {
        g.drawImage(imageIcon.getImage(), (int) currentX, (int) currentY, null);
        notify();
    }
    
    // Make animate for Computer, so player can see computer move the tiles
    synchronized public void doComputerAnimation(GuiOfBoard gui) {
        try {
            gui.setComputerAnimation(this);
            int fromNo = currentPlayer.getPlate(fromPlate) - 1;
            int toNo = currentPlayer.getPlate(toPlate);

            Point fromPoint, toPoint;
            // from Bar
            if (fromPlate == 25) {
                fromPoint = gui.getPointForChip(gui.isPlayerOnTop(currentPlayer) ?
                                                  25 : 0, fromNo);
            } else {
                fromPoint = gui.getPointForChip(currentPlayer.adjustPlate(fromPlate),
                                                  fromNo);
            }

            // to off
            if (toPlate == 0) {
                toPoint = gui.getOutPoint(currentPlayer);
                toPoint.translate(0,
                                  -GuiOfBoard.DIAMETER -
                                  currentPlayer.getOff() * GuiOfBoard.THICKNESS);
            } else {
                toPoint = gui.getPointForChip(currentPlayer.adjustPlate(toPlate), toNo);
            }

            currentX = fromPoint.x;
            currentY = fromPoint.y;
            double distance = toPoint.distance(fromPoint);
            offsetX = (toPoint.x - fromPoint.x) * STEP_LENGTH / distance;
            offsetY = (toPoint.y - fromPoint.y) * STEP_LENGTH / distance;

            int nosteps = (int) (distance / STEP_LENGTH);

            currentPlayer.setDragged(fromPlate);
            for (int i = 0; i < nosteps; i++) {
            	Thread.sleep(SLEEP_TIME);
                currentX += offsetX;
                currentY += offsetY;
                gui.repaint();
                
                wait(); // wait for the animation to be painted
            }
        } catch (InterruptedException ex) {
        } finally {
            gui.setComputerAnimation(null);
            currentPlayer.setDragged( -1);
        }
    }

 // Sleep time between moves.
    private static final long SLEEP_TIME = 5L;
    public static final int STEP_LENGTH = 3;

}
