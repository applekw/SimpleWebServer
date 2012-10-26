/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.Request;
import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.StreamPackage;

/**
 *
 * @author Kyle
 */
public class RequestProcessor implements Runnable {

    private Server server;
    private final long PROCESSING_INTERVAL_MS = (long) 5; //one request is serviced every x ms
////
    public RequestProcessor(Server s) {
        this.server = s;
    }

    public void run() {
        while (true) {
            try {
                Thread.currentThread().sleep(PROCESSING_INTERVAL_MS);
            } catch (InterruptedException ex) {
                Logger.getLogger(RequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            StreamPackage s = this.server.getNextRequest();
            if (s == null) {
                continue;
            }
            Socket socket = s.getSocket();
            long start = s.getStartTime();
            InputStream inStream = null;
            OutputStream outStream = null;

            try {
                inStream = socket.getInputStream();
                outStream = socket.getOutputStream();
            } catch (Exception e) {
                // Cannot do anything if we have exception reading input or output stream
                // May be have text to log this for further analysis?
                e.printStackTrace();

                // Increment number of connections by 1
                server.incrementConnections(1);
                // Get the end time
                long end = System.currentTimeMillis();
                this.server.incrementServiceTime(end - start);
                continue;
            }

            // At this point we have the input and output stream of the socket
            // Now lets create a HttpRequest object
            HttpRequest request = null;
            HttpResponse response = null;
            try {
                request = HttpRequest.read(inStream);
                boolean blacklisted = server.logAndCheckIfBlackListed(request.getUri());
                if (blacklisted) { // the limit on how many requests are processed per second should help prevent the need for a blacklist, but just in case some DOS client can get a ridiculous number or requests in it is here
                    continue;
                }

//			System.out.println(request);
            } catch (ProtocolException pe) {
                // We have some sort of protocol exception. Get its status code and create response
                // We know only two kind of exception is possible inside fromInputStream
                // Protocol.BAD_REQUEST_CODE and Protocol.NOT_IMPLEMENTED_CODE
                int status = pe.getStatus();
                if (status == Protocol.BAD_REQUEST_CODE) {
                    response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
                } else if (status == Protocol.NOT_IMPLEMENTED_CODE) {
                    response = HttpResponseFactory.create501NotImplemented(Protocol.CLOSE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // For any other error, we will create bad request response as well
                response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
            }

            if (response != null) {
                // Means there was an error, now write the response object to the socket
                try {
                    response.write(outStream);
//				System.out.println(response);
                } catch (Exception e) {
                    // We will ignore this exception
                    e.printStackTrace();
                }

                // Increment number of connections by 1
                server.incrementConnections(1);
                // Get the end time
                long end = System.currentTimeMillis();
                this.server.incrementServiceTime(end - start);
                return;
            }

            // We reached here means no error so far, so lets process further
            try {
                // Fill in the code to create a response for version mismatch.
                // You may want to use constants such as Protocol.VERSION, Protocol.NOT_SUPPORTED_CODE, and more.
                // You can check if the version matches as follows
                if (!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
                    // Here you checked that the "Protocol.VERSION" string is not equal to the  
                    // "request.version" string ignoring the case of the letters in both strings
                    // TODO: Fill in the rest of the code here
                } else if (request.getMethod().equalsIgnoreCase(Protocol.GET)) {
//				Map<String, String> header = request.getHeader();
//				String date = header.get("if-modified-since");
//				String hostName = header.get("host");
//				
                    // Handling GET request here
                    // Get relative URI path from request
                    String uri = request.getUri();
                    // Get root directory path from server
                    String rootDirectory = server.getRootDirectory();
                    // Combine them together to form absolute file path
                    File file = new File(rootDirectory + uri);
                    // Check if the file exists
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            // Look for default index.html file in a directory
                            String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
                            file = new File(location);
                            if (file.exists()) {
                                // Lets create 200 OK response
                                response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
                            } else {
                                // File does not exist so lets create 404 file not found code
                                response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
                            }
                        } else { // Its a file
                            // Lets create 200 OK response
                            response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
                        }
                    } else {
                        // File does not exist so lets create 404 file not found code
                        response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            // TODO: So far response could be null for protocol version mismatch.
            // So this is a temporary patch for that problem and should be removed
            // after a response object is created for protocol version mismatch.
            if (response == null) {
                response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
            }

            try {
                // Write response and we are all done so close the socket
                response.write(outStream);
//			System.out.println(response);
                socket.close();
            } catch (Exception e) {
                // We will ignore this exception
                e.printStackTrace();
            }

            // Increment number of connections by 1
            server.incrementConnections(1);
            // Get the end time
            long end = System.currentTimeMillis();
            this.server.incrementServiceTime(end - start);
        }
    }
}

