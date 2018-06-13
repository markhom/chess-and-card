/**
 * Juice
 * com.juice.orange.game.server
 * JuiceServers.java
 */
package com.linyun.bottom.server;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

/**
 * @author shaojieque
 * 2013-3-21
 */
public class JuiceServers {
	/**
     * Returns a new {@link JuiceServer} object, which runs on the provided port.
     */
    public static IJuiceServer createWebServer(int port) {
        return new JuiceServer(port);
    }

    /**
     * Returns a new {@link JuiceServer} object, which runs on the provided port
     * and adds the executor to the List of executor services to be called when
     * the server is running.
     */
    public static IJuiceServer createWebServer(Executor executor, int port) {
        return new JuiceServer(executor, port);
    }

    /**
     * Returns a new {@link JuiceServer} object, adding the executor to the list
     * of executor services, running on the stated socket address and accessible
     * from the provided public URI.
     */
    public static IJuiceServer createWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        return new JuiceServer(executor, socketAddress, publicUri);
    }
}
