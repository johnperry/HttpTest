/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A program to help determine how to make an HTTP connection to
 * the internet. It provides a user interface allowing several
 * different methods to be tested, including:
 * <ul>
 *  <li>HttpURLConnection without a proxy server</li>
 *  <li>HttpURLConnection through a proxy server without authentication</li>
 *  <li>HttpURLConnection through a proxy server with authentication</li>
 *  <li>HttpsURLConnection (Secure Sockets Layer)</li>
 * </ul>
 * This program is intended to be used when an attempt to connect
 * a clinical trial field center to a principal investigator site
 * fails and it is not clear why.
 */
public class HttpTest extends JFrame implements ChangeListener {

	private String windowTitle = "MIRC HttpTest - version 5";

	JTabbedPane tabbedPane;
	JPanel main;
	Authorization authorization;
	Proxy proxy;
	HttpClient client;
	HttpServer server;
	IPPane ipPane;
	public static Message message;

	public static void main(String args[]) {
		new HttpTest();
	}

	/**
	 * Class constructor.
	 */
	public HttpTest() {
		setTitle(windowTitle);
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) { exitForm(evt); } });

		main = new JPanel(new BorderLayout());
		client = new HttpClient();
		server = new HttpServer();
		ipPane = new IPPane();
		SysProps sysProps = new SysProps();
		EnvProps envProps = new EnvProps();
 		Instructions instructions = new Instructions();
		proxy = new Proxy();
		authorization = new Authorization();
		message = new Message();

		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		tabbedPane.addTab("HTTP Client",client);
		tabbedPane.addTab("Authentication",authorization);
		tabbedPane.addTab("Proxy",proxy);
		tabbedPane.addTab("HTTP Server",server);
		tabbedPane.addTab("IP Address",ipPane);
		tabbedPane.addTab("SysProps",sysProps);
		tabbedPane.addTab("EnvProps",envProps);
		tabbedPane.addTab("Help",instructions);

		main.add(tabbedPane, BorderLayout.CENTER);
		main.add(message, BorderLayout.SOUTH);
		getContentPane().add(main,BorderLayout.CENTER);

		pack();
		Toolkit t = getToolkit();
		Dimension scr = t.getScreenSize ();
		setSize(scr.width/2, 9*scr.height/10);
		setLocation (new Point ((scr.width-getSize().width)/2,(scr.height-getSize().height)/2));
		setVisible(true);
	}

	/**
	 * The ChangeListener interface implementation.
	 * This method listens for ChangeEvents and interprets them
	 * as tab selections in the JTabbedPane. It notifies the
	 * component associated with the currently selected tab that
	 * it has been activated.
	 * @param event the event.
	 */
	public void stateChanged(ChangeEvent event) {
		Component c = tabbedPane.getSelectedComponent();
		if ((c != null) && (c instanceof Activatable)) {
			((Activatable)c).activate();
			int i = tabbedPane.getSelectedIndex();
		}
	}

	private void exitForm(java.awt.event.WindowEvent evt) {
		System.exit(0);
	}

}
