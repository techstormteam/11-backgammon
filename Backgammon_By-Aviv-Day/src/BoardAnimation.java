
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
public class BoardAnimation extends Thread {

    private Player player;
    private ImageIcon chip;
    private int fromJag;
    private int toJag;

    private double curX, curY;
    private double offsetX, offsetY;

    public static final int STEPLENGTH = Integer.getInteger(
            "jgam.animationstep", 1).intValue();

    // Sleep time between moves.
    private static final long SLEEPTIME = Integer.getInteger(
            "jgam.animationdelay", 5).intValue();

    public BoardAnimation(Player player, int from, int to) {
        this.player = player;
        this.chip = player.getChipIcon();

        fromJag = from;
        toJag = to;
    }

    
    // Make animate for Computer, so player can see computer move the tiles
    synchronized public void animate(Board board) {
        try {
            board.setAnimation(this);
            int fromNo = player.getJag(fromJag) - 1;
            int toNo = player.getJag(toJag);

            Point fromPoint, toPoint;
            // from Bar
            if (fromJag == 25) {
                fromPoint = board.getPointForChip(board.isPlayerOnTop(player) ?
                                                  25 : 0, fromNo);
            } else {
                fromPoint = board.getPointForChip(player.adjustJag(fromJag),
                                                  fromNo);
            }

            // to off
            if (toJag == 0) {
                toPoint = board.getOutField(player);
                toPoint.translate(0,
                                  -Board.CHIPDIAMETER -
                                  player.getOff() * Board.CHIPTHICKNESS);
            } else {
                toPoint = board.getPointForChip(player.adjustJag(toJag), toNo);
            }

            curX = fromPoint.x;
            curY = fromPoint.y;
            double distance = toPoint.distance(fromPoint);
            offsetX = (toPoint.x - fromPoint.x) * STEPLENGTH / distance;
            offsetY = (toPoint.y - fromPoint.y) * STEPLENGTH / distance;

            int nosteps = (int) (distance / STEPLENGTH);

            player.setDragged(fromJag);
            for (int i = 0; i < nosteps; i++) {
            	Thread.sleep(SLEEPTIME);
                curX += offsetX;
                curY += offsetY;
                board.repaint();
                
                wait(); // wait for the animation to be painted
            }
        } catch (InterruptedException ex) {
        } finally {
            board.setAnimation(null);
            player.setDragged( -1);
        }
    }

    // Draw chip icon
    synchronized public void paint(Graphics g) {
        g.drawImage(chip.getImage(), (int) curX, (int) curY, null);
        notify();
    }

}
