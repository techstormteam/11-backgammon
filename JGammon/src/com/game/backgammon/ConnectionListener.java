package com.game.backgammon;

/**
 * a connectionListener handles Messages that came over the
 * connection
 *
 * @author Aviv
 * @version 1.0
 */
public interface ConnectionListener {

        /**
         * handle a ConnectionMessage, which is either a Message or an
         * exception.
         *
         * @param cm ConnectionMessage to be handled
         */
        public void handleConnectionMessage(ConnectionMessage cm);
}
