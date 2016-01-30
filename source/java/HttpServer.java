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
		public JButton clear;
		boolean running = false;
		Dimension buttonSize;
		String defaultPort = "5678";

		public Header() {
			super();
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.setBorder(BorderFactory.createEmptyBorder(2,0,4,0));
			port = new JTextField(defaultPort,6);
			port.setFont(font);
			Dimension portSize = port.getPreferredSize();
			port.setMaximumSize(portSize);
			startStop = new JButton("Start");
			buttonSize = startStop.getPreferredSize();
			startStop.addActionListener(this);
			sendResponse = new JCheckBox("Send text response page");
			sendResponse.setSelected(true);
			clear = new JButton("Clear");
			clear.addActionListener(this);
			running = false;

			this.add(Box.createHorizontalStrut(5));
			this.add(new JLabel("Port:"));
			this.add(Box.createHorizontalStrut(3));
			this.add(port);
			this.add(Box.createHorizontalStrut(10));
			this.add(startStop);
			this.add(Box.createHorizontalStrut(15));
			this.add(sendResponse);
			this.add(Box.createHorizontalGlue());
			this.add(clear);
			this.add(Box.createHorizontalStrut(5));
		}

		public void httpConnectionEventOccurred(HttpConnectionEvent event) {
			int len = editor.getDocument().getLength(); // same value as getText().length();
			editor.setCaretPosition(len);  // place caret at the end (with no selection)
			editor.replaceSelection(event.message); // there is no selection, so inserts at caret
		}

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source.equals(startStop)) {
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
			else if (source.equals(clear)) {
				editor.setText("");
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
