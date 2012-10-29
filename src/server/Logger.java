/*
 * Logger.java
 * Oct 29, 2012
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
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
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */
 
package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import protocol.Protocol;

/**
 * 
 * @author Greg 
 */
public class Logger {

	private static final String FILE_NAME = "ServerLog_";
	private static final String FILE_EXT = ".csv";
	private File file;
	private FileWriter writer;
	
	public Logger()
	{
		Calendar calendar = Calendar.getInstance(); 
		String date = calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "_" 
						+ calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE);
		File log = new File(FILE_NAME + date + FILE_EXT);
		// use existing file
		if (log.exists())
		{
			this.file = log;
		}
		// make a new one
		else 
		{
			try {
				log.createNewFile();
				this.file = log;
				this.writer = new FileWriter(this.file);
				this.writer.write("onewaylat,turnaround" + Protocol.CRLF);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	
	public <T> void log(T text)
	{
		try {
			this.writer.write(text + ",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void logNewline(T text)
	{
		try {
			this.writer.write(text + "," + Protocol.CRLF);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
