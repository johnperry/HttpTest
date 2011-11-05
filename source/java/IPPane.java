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
 * A JPanel providing the list of IP addresses known by the OS.
 */
public class IPPane extends JPanel {

	Header header;
	JScrollPane scroller;
	ScrollableEditorPane editor;
	Font font;
	JButton getIPs;

	/**
	 * Class constructor; provides the user interface and the actual
	 * communication code.
	 */
	public IPPane() {
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
	class Header extends JPanel implements ActionListener {
		public JButton getIPs;

		public Header() {
			super();
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			getIPs = new JButton("Get IP Addresses");
			getIPs.addActionListener(this);
			this.add(getIPs);
		}

		public void actionPerformed(ActionEvent e) {
			StringBuffer sb = new StringBuffer();
			try {
				//Get all the network interfaces
				Enumeration<NetworkInterface> nwEnum = NetworkInterface.getNetworkInterfaces();
				while (nwEnum.hasMoreElements()) {
					NetworkInterface nw = nwEnum.nextElement();
					sb.append("\nNetwork Interface: "+nw.getDisplayName()+"\n");
					Enumeration<InetAddress> ipEnum = nw.getInetAddresses();
					while (ipEnum.hasMoreElements()) {
						InetAddress ina = ipEnum.nextElement();
						sb.append("    " + ina.getHostAddress() + "\n");
						if (ina instanceof Inet4Address) sb.append("      - Inet4\n");
						if (ina.isLoopbackAddress()) sb.append("      - loopback address\n");
					}
				}
			}
			catch (Exception ex) {
				sb.append("Unable to obtain the IP addresses");
			}
			editor.setText(sb.toString());
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
