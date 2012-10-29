/*
 * Server.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 */
package server;

import gui.WebServer;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import protocol.HttpRequest;
import protocol.StreamPackage;

/**
 * This represents a welcoming server for the incoming
 * TCP request from a HTTP client such as a web browser. 
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class Server implements Runnable {

    private String rootDirectory;
    private int port;
    private String username;
    private String password;
    private boolean stop;
    private boolean kill;
    private ServerSocket welcomeSocket;
    private long connections;
    private long serviceTime;
    public HashMap<String, ArrayList<Long>> connectionLog;
    public HashSet<String> blacklist;
    private Logger log;
    private WebServer window;
    private Queue<StreamPackage> requestQueue;
   
    private  static final int CONNECTION_NUM_INTERVAL = 3;
    private static final int CONNECTION_MS_INTERVAL = 500; 

    /**
     * @param rootDirectory
     * @param port
     */
    public Server(String rootDirectory, int port, WebServer window) {
        this.rootDirectory = rootDirectory;
        this.port = port;
        this.username = null;
        this.password = null;
        this.stop = false;
        this.kill = false;
        this.connections = 0;
        this.serviceTime = 0;
        this.window = window;
	this.log = new Logger();

        this.connectionLog = new HashMap<String, ArrayList<Long>>();
        this.blacklist = new HashSet<String>();
        this.requestQueue = new LinkedList<StreamPackage>();
    }
    
    public Server(String rootDirectory, int port, String username, String password, WebServer window) {
        this.rootDirectory = rootDirectory;
        this.port = port;
        this.username = username;
        this.password = password;
        this.stop = false;
        this.kill = false;
        this.connections = 0;
        this.serviceTime = 0;
        this.window = window;

        this.connectionLog = new HashMap<String, ArrayList<Long>>();
        this.blacklist = new HashSet<String>();
        this.requestQueue = new LinkedList<StreamPackage>();
    }

    /**
     * Gets the root directory for this web server.
     * 
     * @return the rootDirectory
     */
    public String getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Gets the port number for this web server.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns connections serviced per second. 
     * Synchronized to be used in threaded environment.
     * 
     * @return
     */
    public synchronized double getServiceRate() {
        if (this.serviceTime == 0) {
            return Long.MIN_VALUE;
        }
        double rate = this.connections / (double) this.serviceTime;
        rate = rate * 1000;
        return rate;
    }

    /**
     * Increments number of connection by the supplied value.
     * Synchronized to be used in threaded environment.
     * 
     * @param value
     */
    public synchronized void incrementConnections(long value) {
        this.connections += value;
    }

    /**
     * Increments the service time by the supplied value.
     * Synchronized to be used in threaded environment.
     * 
     * @param value
     */
    public synchronized void incrementServiceTime(long value) {
	this.log.logNewline(value);
        this.serviceTime += value;
    }

    /**
     * The entry method for the main server thread that accepts incoming
     * TCP connection request and creates a {@link ConnectionHandler} for
     * the request.
     */
    public void run() {
        try {
            this.welcomeSocket = new ServerSocket(port);
            RequestProcessor processor = new RequestProcessor(this);
         
            new Thread(processor).start();
            // Now keep welcoming new connections until stop flag is set to true
            while (true) {
                // Listen for incoming socket connection
                // This method block until somebody makes a request
                Socket connectionSocket = this.welcomeSocket.accept();

                // Come out of the loop if the stop flag is set
                if (this.stop || this.kill) {
                    break;
                }

                // Create a handler for this incoming connection and start the handler in a new thread
                ConnectionHandler handler = new ConnectionHandler(this, connectionSocket);
                
                new Thread(handler).start();
            }
            this.welcomeSocket.close();
            while (!this.requestQueue.isEmpty())
            {
              if (this.kill) break;
            }
            
        } catch (Exception e) {
            window.showSocketException(e);
        }
        
    }

    /**
     * Stops the server from listening further.
     */
    public synchronized void stop() {
        if (this.stop) {
            return;
        }

        // Set the stop flag to be true
        this.stop = true;
        try {
            // This will force welcomeSocket to come out of the blocked accept() method 
            // in the main loop of the start() method
            Socket socket = new Socket(InetAddress.getLocalHost(), port);

            // We do not have any other job for this socket so just close it
            socket.close();
	    this.log.close();
        } catch (Exception e) {
        }
    }
    
     /**
     * Stops the server from listening and/or processing queued requests
     */
    public synchronized void kill() {
        if (this.kill) {
            return;
        }

        // Set the stop flag to be true and empty the request queue
        this.kill = true;
        this.requestQueue.clear();
        try {
            // This will force welcomeSocket to come out of the blocked accept() method 
            // in the main loop of the start() method
            Socket socket = new Socket(InetAddress.getLocalHost(), port);

            // We do not have any other job for this socket so just close it
            socket.close();
	    this.log.close();
        } catch (Exception e) {
        }
    }

    /**
     * Checks if the server is stopped or not.
     * @return
     */
    public boolean isStopped() {
        if (this.welcomeSocket != null) {
            return this.welcomeSocket.isClosed();
        }
        return true;
    }
    
    public boolean isKilled()
    {
         return this.kill;
    }

    //logs the given uri and the current system time. Returns true if the uri is blacklisted, false otherwise.
    boolean logAndCheckIfBlackListed(String uri) {
       if (this.blacklist.contains(uri)) return true;
        if (!this.connectionLog.containsKey(uri)) {
            this.connectionLog.put(uri, new ArrayList<Long>());
        }
        ArrayList<Long> times = this.connectionLog.get(uri);
        times.add(System.currentTimeMillis());
        this.connectionLog.put(uri, times);
        
       
        if (times.size() >= CONNECTION_NUM_INTERVAL && times.get(times.size() - 1) - times.get(0) <= CONNECTION_MS_INTERVAL) {
            this.blacklist.add(uri);
            return true;
        }

        return false;
    }

    public StreamPackage getNextRequest() {
       return this.requestQueue.poll();
    }
    
    public void addRequest(StreamPackage r)
    {
    if (this.requestQueue.size() <= 10) {
        this.requestQueue.add(r);
        }
    }
   
    public Logger getLog()
    {
        return this.log;
    }
    
    
}
