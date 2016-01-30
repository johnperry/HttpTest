/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.io.*;

/**
 * A simple HTTP text response.
 */
public class HttpResponse {

	StringBuffer response = null;
	String extraHeaders = "";

	/**
	 * Class constructor; creates an HTTP text response.
	 */
	public HttpResponse() {
		response = new StringBuffer();
	}
	
	/**
	 * Set an extra header in the response.
	 * @param string the string to append.
	 */
	public void setHeader(String name, String value) {
		extraHeaders += name + ": " + value + "\r\n";
	}

	/**
	 * Append a string to the response.
	 * @param string the string to append.
	 */
	public void write(String string) {
		response.append(string);
	}

	/**
	 * Send the response, prepending only the normal headers.
	 * @param stream the socket's output stream.
	 */
	public void send(OutputStream stream) {
		String headers =
			"HTTP/1.0 200 OK\r\n" +
			"Content-Type: text/plain\r\n" +
			extraHeaders +
			"Content-Length: " + response.length() + "\r\n\r\n";

		try {
			stream.write(headers.getBytes());
			if (response.length() > 0) {
				stream.write(response.toString().getBytes());
			}
		}
		catch (Exception ex) {
			System.out.println("Exception while writing the response\n"+ex);
		}
	}
}


