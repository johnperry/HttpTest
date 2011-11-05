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

	/**
	 * Class constructor; creates an HTTP text response.
	 */
	public HttpResponse() {
		response = new StringBuffer();
	}

	/**
	 * Append a string to the reponse.
	 * @param string the string to append.
	 */
	public void write(String string) {
		response.append(string);
	}

	/**
	 * Send the response, prepending the appropriate headers.
	 * @param stream the socket's output stream.
	 */
	public void send(OutputStream stream) {
		String headers =
			"HTTP/1.0 200 OK\r\n" +
			"Content-Type: text/plain\r\n" +
			"Content-Length: " + response.length() + "\r\n\r\n";

		try {
			stream.write(headers.getBytes());
			stream.write(response.toString().getBytes());
		}
		catch (Exception ex) {
			System.out.println("Exception while writing the response\n"+ex);
		}
	}
}


