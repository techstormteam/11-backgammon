/*
 * An object that has a string representation and belongs to a player
 * @author Aviv
 */
public interface PlayerObject {

    /*
     * get the owner of this object
     *
     * @return Player to which this belongs
     */
    public Player player();

    /*
     * set the owner of this.
     * OPtional operation
     * @param player the owner to be set
     */
    public void setPlayer(Player player);

}
