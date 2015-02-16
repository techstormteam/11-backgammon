
/**
 * A Text-Message that will be printed in the history window.
 *
 * @author not attributable
 * @version 1.0
 */
public class History implements PlayerObject {

    private String message;
    private Player player;


    /**
     *
     * @return Player to which this belongs
     * @todo Implement this jgam.PlayerOwnedObject method
     */
    public Player player() {
        return player;
    }

    /**
     * set the owner of this.
     *
     * @param player the owner to be set
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * toString
     *
     * @return String
     * @todo Implement this jgam.PlayerOwnedObject method
     */
    public String toString() {
        return message;
    }

    /**
     * create a special Move with a given specialString that is given by
     * an integer which describes an action of a player
     * @param player Player to associate with
     */
    public History(Player player) {
        this.player = player;
    }

}
