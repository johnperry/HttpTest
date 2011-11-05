/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import org.rsna.util.*;

/**
 * A JPanel for managing proxy server parameters. This class provides
 * a user interface allowing for enabling and disabling the use of
 * the proxy server, for specifying the IP address and port of the
 * proxy server, and for specifying the authentication credentials
 * required by the proxy server. It also provides methods for setting
 * and removing the Java System properties required for a proxy server.
 */
public class Proxy extends JPanel {

	private static JCheckBox enabled;
	private static JTextField ip;
	private static JTextField port;
	private static JTextField user;
	private static JTextField password;

	/**
	 * Class constructor; creates an instance of the Proxy class
	 * and initializes it to disable the use of the proxy server.
	 */
	public Proxy() {
		super();
		Border border = BorderFactory.createEmptyBorder(10,10,10,10);
		this.setBorder(border);
		this.setLayout(new SpecialLayout(10,10));

		enabled = new JCheckBox("Use Proxy Server for client connections", false);
		JLabel ipLabel = new JLabel("IP:");
		ip = new JTextField(15);
		JLabel portLabel = new JLabel("Port:");
		port = new JTextField(4);
		JLabel userLabel = new JLabel("Username:");
		user = new JTextField(10);
		JLabel passwordLabel = new JLabel("Password:");
		password = new JTextField(10);

		this.add(enabled);
		this.add(ipLabel);
		this.add(ip);
		this.add(portLabel);
		this.add(port);
		this.add(userLabel);
		this.add(user);
		this.add(passwordLabel);
		this.add(password);
	}

	/**
	 * Get the status of the proxy server checkbox.
	 * @return true if the proxy server checkbox is selected; otherwise false.
	 */
	public static boolean getEnabled() {
		return enabled.isSelected();
	}

	/**
	 * Get the contents of the IP address text field.
	 * @return the contents of the IP address text field.
	 */
	public static String getIP() {
		return ip.getText().trim();
	}

	/**
	 * Get the contents of the Port text field.
	 * @return the contents of the Port text field.
	 */
	public static String getPort() {
		return port.getText().trim();
	}

	/**
	 * Get the contents of the User text field.
	 * @return the contents of the User text field.
	 */
	public static String getUser() {
		return user.getText().trim();
	}

	/**
	 * Get the contents of the Password text field.
	 * @return the contents of the Password text field.
	 */
	public static String getPassword() {
		return password.getText().trim();
	}

	/**
	 * Set the Java System properties that apply to the proxy server
	 * using the values in the user interface. The properties set are:
	 * <ul>
	 *  <li>proxySet = true</li>
	 *  <li>http.proxyHost = getIP()</li>
	 *  <li>http.proxyPort = getPort()</li>
	 * </ul>
	 */
	public static void setProxyProperties() {
		System.setProperty("proxySet","true");
		System.setProperty("http.proxyHost",getIP());
		System.setProperty("http.proxyPort",getPort());
	}

	/**
	 * Clear the Java System properties that apply to the proxy server.
	 * The properties cleared are:
	 * <ul>
	 *  <li>proxySet</li>
	 *  <li>http.proxyHost</li>
	 *  <li>http.proxyPort</li>
	 * </ul>
	 */
	public static void clearProxyProperties() {
		Properties sys = System.getProperties();
		sys.remove("proxySet");
		sys.remove("http.proxyHost");
		sys.remove("http.proxyPort");
	}

	/**
	 * Determine whether the current values in the user interface indicate
	 * that proxy user authentication is to be used.
	 * @return true if both Proxy User and Proxy Password are not blank; false otherwise.
	 */
	public static boolean authenticate() {
		if (!getUser().equals("") && !getPassword().equals("")) return true;
		return false;
	}

	/**
	 * Get the base-64 encoded value of the credentials
	 * in the form required for an HTTP Proxy-Authorization header:
	 * <ul><li>
	 * encode((getUser() + ":" + getPassword()).getBytes())
	 * </li></ul>
	 * @return the user authentication credentials from the user interface.
	 */
	public static String getEncodedCredentials() {
		return Base64.encodeToString((getUser() + ":" + getPassword()).getBytes());
	}

	//Special layout manager for the proxy panel
	class SpecialLayout implements LayoutManager {

		private int horizontalGap;
		private int verticalGap;

		public SpecialLayout(int horizontalGap, int verticalGap) {
			this.horizontalGap = horizontalGap;
			this.verticalGap = verticalGap;
		}

		public void addLayoutComponent(String name,Component component) {
			//not necessary for this layout manager
		}

		public void removeLayoutComponent(Component component) {
		}

		public Dimension preferredLayoutSize(Container parent) {
			return getLayoutSize(parent,horizontalGap,verticalGap,false);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return getLayoutSize(parent,horizontalGap,verticalGap,false);
		}

		public void layoutContainer(Container parent) {
			getLayoutSize(parent,horizontalGap,verticalGap,true);
		}

		private Dimension getLayoutSize(Container parent, int hGap, int vGap, boolean layout) {
			int width = 0;
			int height = 0;
			Component p = parent.getParent();
			if (p instanceof JScrollPane)
				width = ((JScrollPane)p).getViewport().getExtentSize().width;
			else
				width = parent.getSize().width;
			Component[] components = parent.getComponents();
			Insets insets = parent.getInsets();
			int currentY = insets.top;
			Dimension d0, d1;

			if (components.length == 0)
				return new Dimension(
					insets.left + insets.right, insets.top + insets.bottom);

			//Okay, there is something in the container.
			//Lay out the first component on its own line
			d0 = components[0].getPreferredSize();
			components[0].setBounds(insets.left,currentY,d0.width,d0.height);
			currentY += d0.height + vGap;

			//Now lay out the rows of labels + textfields.
			//First make the labels all the same width.
			//Find the maximum width of the labels.
			int w;
			int labelWidth = 0;
			for (int i=1; i<components.length-1; i+=2) {
				w = components[i].getPreferredSize().width;
				labelWidth = Math.max(w, labelWidth);
			}

			for (int i=1; i<components.length-1; i+=2) {
				int currentX = insets.left;
				d0 = components[i].getPreferredSize();
				d1 = components[i+1].getPreferredSize();
				//put the components on the line
				if (layout) {
					components[i].setBounds(currentX, currentY, labelWidth, d0.height);
					int x1 = currentX + labelWidth + hGap;
					int w1 = width - x1 - insets.right;
					components[i+1].setBounds(x1, currentY, w1, d1.height);
				}
				currentY += vGap + Math.max(d0.height, d1.height);
			}
			return new Dimension(width, currentY+insets.bottom);
		}
	}

}
