/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import javax.net.ServerSocketFactory;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import java.util.EventListener;

/**
 * A simple receiver for DICOM objects transmitted using the HTTP protocol.
 */
public class HttpReceiver extends Thread implements HttpConnectionEventListener {

	File dir;
	int port;
	boolean sendResponse;
	String protocol = "http";
	ServerSocket serverSocket;
	EventListenerList listenerList;

	/**
	 * Class constructor; creates a new instance of the HttpReceiver.
	 * @param port the port on which to listen for file transfers.
	 * @param sendResponse true if the server is to send a report response
	 * to the client when a connection is received, false otherwise.
	 */
    public HttpReceiver (int port, boolean sendResponse) throws Exception {

		listenerList = new EventListenerList();
		this.dir = dir;
		this.port = port;
		this.sendResponse = sendResponse;

		//Now get the Server Socket on the specified port.
		//Note: this will throw an Exception if it fails.
		ServerSocketFactory serverSocketFactory = ServerSocketFactory.getDefault();
		serverSocket = serverSocketFactory.createServerSocket(port);
	}

	/**
	 * Accept connections and receive files, sending HttpFileEvents when
	 * files have been placed in the directory.
	 */
	public void run() {
		//Send a startup message
		sendHttpConnectionEvent(
			this,HttpConnectionEvent.STARTUP,"Server started; port open");

		//Accept connections and handle them
		while (!this.isInterrupted()) {
			try {
				//Wait for a connection
				Socket socket = serverSocket.accept();

				//Create a thread to handle the connection
				HttpHandler handler = new HttpHandler(socket,sendResponse);
				handler.addHttpConnectionEventListener(this);
				handler.start();
			}
			catch (Exception ex) { break; }
		}
	}

	/**
	 * Stop the HttpReceiver.
	 */
	public void stopReceiver() {
		try { serverSocket.close(); }
		catch (Exception ignore) { }
		this.interrupt();
		sendHttpConnectionEvent(
			this,HttpConnectionEvent.SHUTDOWN,"Server shut down; port closed");
	}

	/**
	 * Forward events from the subordinate threads that receive connections
	 * to the HttpEventListeners that are registered with the HttpReceiver.
	 * @param event the event that identifying the file that was received.
	 */
	public void httpConnectionEventOccurred(HttpConnectionEvent event) {
		sendHttpConnectionEvent(this, event.status, event.message);
	}

	/**
	 * Add an HttpConnectionEventListener to the listener list.
	 * @param listener the HttpFileEventListener.
	 */
	public void addHttpConnectionEventListener(HttpConnectionEventListener listener) {
		listenerList.add(HttpConnectionEventListener.class, listener);
	}

	/**
	 * Remove an HttpConnectionEventListener from the listener list.
	 * @param listener the HttpFileEventListener.
	 */
	public void removeHttpConnectionEventListener(HttpConnectionEventListener listener) {
		listenerList.remove(HttpConnectionEventListener.class, listener);
	}

	//Send an HttpConnectionEvent to all HttpConnectionEventListeners.
	//The event is sent in the event thread to make it safe for GUI components.
	private void sendHttpConnectionEvent(Object object, int status, String message) {
		final EventListener[] listeners = listenerList.getListeners(HttpConnectionEventListener.class);
		if (listeners.length > 0) {
			final HttpConnectionEvent event = new HttpConnectionEvent(object, status, message);
			Runnable fireEvents = new Runnable() {
				public void run() {
					for (int i=0; i<listeners.length; i++) {
						((HttpConnectionEventListener)listeners[i]).httpConnectionEventOccurred(event);
					}
				}
			};
			SwingUtilities.invokeLater(fireEvents);
		}
	}

}
