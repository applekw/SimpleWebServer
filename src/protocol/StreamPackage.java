/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Kyle
 */
public class StreamPackage {
    private Socket socket;

    private long startTime;
    
    public StreamPackage(Socket s , long t)
    {
        this.socket = s;
        this.startTime = t;
    }

 
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
}
