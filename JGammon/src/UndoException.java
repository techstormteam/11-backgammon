
/**
 *
 * Used to handle Undo-Requests
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class UndoException extends Exception {

    /**
     * this is true if a message is to be sent to the opponent (if there is
     * a connection
     * this is false if this is the reaction upon such a message and no
     * new message is to be generated;
     */
    private boolean sendMessage = false;

    public UndoException(boolean sendMessage) {
        this.sendMessage = sendMessage;
    }

    public boolean sendMessage() {
        return sendMessage;
    }
}
