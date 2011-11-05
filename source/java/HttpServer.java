/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * A JPanel providing a user interface and communication software
 * for an HTTP (not HTTPS) server that logs connection information.
 */
public class HttpServer extends JPanel {

	Header header;
	JScrollPane scroller;
	ScrollableEditorPane editor;
	Font font;
	HttpReceiver httpReceiver;

	/**
	 * Class constructor; provides the user interface and the actual
	 * communication code.
	 */
	public HttpServer() {
		super(new BorderLayout());
		font = new Font("Monospaced", Font.PLAIN, 12);
		header = new Header();
		this.add(header,BorderLayout.NORTH);
		scroller = new JScrollPane();
		editor = new ScrollableEditorPane("text/plain","");
		scroller.setViewportView(editor);
		this.add(scroller,BorderLayout.CENTER);
		editor.setFont(font);
	}

	//Class to provide the buttons and text selections
	class Header extends JPanel implements ActionListener,
										   HttpConnectionEventListener {
		public JTextField port;
		public JButton startStop;
		public JCheckBox sendResponse;
		boolean running = false;
		Dimension buttonSize;
		String defaultPort = "5678";

		public Header() {
			super();
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			port = new JTextField(defaultPort,6);
			port.setFont(font);
			startStop = new JButton("Start");
			buttonSize = startStop.getPreferredSize();
			startStop.addActionListener(this);
			sendResponse = new JCheckBox("Send text response page");
			running = false;

			this.add(new JLabel("Port:"));
			this.add(Box.createHorizontalStrut(2));
			this.add(port);
			this.add(Box.createHorizontalStrut(10));
			this.add(startStop);
			this.add(Box.createHorizontalStrut(15));
			this.add(sendResponse);
		}

		public void httpConnectionEventOccurred(HttpConnectionEvent event) {
			editor.setText(event.message);
		}

		public void actionPerformed(ActionEvent e) {
			if (!running) {
				//Start the server
				String portText = port.getText().trim();
				try {
					int portNumber = Integer.parseInt(portText);
					httpReceiver = new HttpReceiver(portNumber,sendResponse.isSelected());
					httpReceiver.addHttpConnectionEventListener(this);
					httpReceiver.start();
					startStop.setText("Stop");
					startStop.setPreferredSize(buttonSize);
					running = true;
				}
				catch (Exception ex) {
					HttpTest.message.setText("Unable to start the server");
				}
			}
			else {
				//Stop the server
				if (httpReceiver != null) httpReceiver.stopReceiver();
				httpReceiver = null;
				startStop.setText("Start");
				startStop.setPreferredSize(buttonSize);
				running = false;
			}
		}
	}

	class ScrollableEditorPane extends JEditorPane {
		public ScrollableEditorPane(String contentType, String text) {
			super(contentType,text);
		}
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
	}

}
