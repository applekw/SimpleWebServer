/*
 * ProtocolException.java
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
 
package protocol;

/**
 * This class is used to represent exceptions in processing
 * HTTP protocol.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ProtocolException extends Exception {
	private static final long serialVersionUID = -2475212356774585742L;
	
	private int status;

	/**
	 * Creates exception object with default message and default code.
	 */
	public ProtocolException() {
		super("An error occured while executing http protocol.");
		this.status = Protocol.NOT_SUPPORTED_CODE;
	}

	/**
	 * Creates exception object with supplied exception message.
	 * 
	 * @param message The message for the exception.
	 */
	public ProtocolException(String message) {
		super(message);
		this.status = Protocol.NOT_SUPPORTED_CODE;
	}

	/**
	 * Creates exception object with supplied exception message.
	 * 
	 * @param status The status code for the exception.
	 * @param message The message for the exception.
	 */
	public ProtocolException(int status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * Creates exception object generated due to another exception.
	 * 
	 * @param cause The cause object for this exception to occur.
	 */
	public ProtocolException(Throwable cause) {
		super(cause);
		status = Protocol.NOT_SUPPORTED_CODE;
	}

	/**
	 * Creates exception object with supplied message and cause.
	 * 
	 * @param message The message.
	 * @param cause The cause exception object.
	 */
	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
		status = Protocol.NOT_SUPPORTED_CODE;
	}
	
	public int getStatus() {
		return status;
	}
}
