
/**
 * A Text-Message that will be printed in the history window.
 *
 * @author not attributable
 * @version 1.0
 */
public class HistoryMessage implements PlayerOwnedObject {

    private String text;
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
        return text;
    }

    /**
     * create a message with the given string
     * @param string given message
     * @param player Player to associate with
     */
    public HistoryMessage(String text, Player player) {
        this.text = text;
        this.player = player;

    }

    /**
     * create a special Move with a given specialString that is given by
     * an integer which describes an action of a player
     * @param special DOUBLE or a GIVE_UP_*
     * @param player Player to associate with
     */
    public HistoryMessage(int special, Player player) {
        switch (special) {
        case Player.DOUBLE:
            this.text = "Propose DOUBLE";
            break;
        case Player.GIVE_UP:
            this.text = "Propose ORDINARY resignation";
            break;
        case Player.GIVE_UP_GAMMON:
            this.text = "Propos GAMMON resignation";
            break;
        case Player.GIVE_UP_BACKGAMMON:
            this.text = "Propose BACKGAMMON resignation";
            break;
        default:
            throw new IllegalArgumentException("" + special + " not allowed");
        }
        this.player = player;
    }

}
