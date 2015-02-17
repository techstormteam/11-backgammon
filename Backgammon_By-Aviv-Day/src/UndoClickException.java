
/*
 *
 * Used to handle Undo-Requests
 *
 * @author Aviv
 * @version 1.0
 */
public class UndoClickException extends Exception {

    /*
     * this is true if a message is to be sent to the opponent (if there is
     * a connection
     * this is false if this is the reaction upon such a message and no
     * new message is to be generated;
     */
    private boolean message = false;

    public UndoClickException(boolean sendMessage) {
        this.message = sendMessage;
    }

    public boolean sendMessage() {
        return message;
    }
}
