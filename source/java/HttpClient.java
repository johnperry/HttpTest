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
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;

/**
 * A JPanel providing a user interface and communication software
 * for trying several ways to  establish an HTTP connection to the internet.
 * <ul>
 *  <li>HttpURLConnection without a proxy server</li>
 *  <li>HttpURLConnection through a proxy server without authentication</li>
 *  <li>HttpURLConnection through a proxy server with authentication</li>
 *  <li>HttpsURLConnection (Secure Sockets Layer)</li>
 * </ul>
 */
public class HttpClient extends JPanel {

	Header header;
	JScrollPane scroller;
	ScrollableEditorPane editor;
	Font font;
	TrustManager[] trustAllCerts;

	/**
	 * Class constructor; provides the user interface and the actual
	 * communication code.
	 */
	public HttpClient() {
		super(new BorderLayout());
		font = new Font("Monospaced", Font.PLAIN, 12);
		header = new Header();
		this.add(header,BorderLayout.NORTH);
		scroller = new JScrollPane();
		editor = new ScrollableEditorPane("text/plain","");
		scroller.setViewportView(editor);
		this.add(scroller,BorderLayout.CENTER);
		editor.setFont(font);

		trustAllCerts = new TrustManager[] { new AcceptAllX509TrustManager() };
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null,trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (Exception e) { }
	}

	//Try to make a connection and display the results
	void connect(String addr) {
		URL url;
		HttpURLConnection conn;

		//If the proxy is enabled, set the Java System properties for it.
		//If it is not enabled, remove the Java System properties for it.
		if (Proxy.getEnabled()) Proxy.setProxyProperties();
		else Proxy.clearProxyProperties();

		//Make the connection
		try {
			url = new URL(addr);
			if (url.getProtocol().toLowerCase().startsWith("https")) {
				//This is a secure sockets layer connection
				HttpsURLConnection httpsConn = (HttpsURLConnection)url.openConnection();
				httpsConn.setHostnameVerifier(new AcceptAllHostnameVerifier());
				httpsConn.setUseCaches(false);
				httpsConn.setDefaultUseCaches(false);
				conn = httpsConn;
			}
			else
				//This is a straight HTTP connection.
				conn = (HttpURLConnection)url.openConnection();

			//Get the method selected (GET vs POST)
			String method = (String)header.getMethod();
			conn.setRequestMethod(method);

			//If the proxy is enabled and authentication credentials are available,
			//set them in the request.
			if (Proxy.getEnabled() && Proxy.authenticate()) {
				conn.setRequestProperty(
					"Proxy-Authorization","Basic "+Proxy.getEncodedCredentials());
			}

			//If the authorization is enabled and authentication credentials are available,
			//set them in the request.
			if (Authorization.getEnabled() && Authorization.authenticate()) {
				conn.setRequestProperty(
					"Authorization","Basic "+Authorization.getEncodedCredentials());
			}

			//Make the connection
			conn.connect();

			//And display the results.
			editor.setText(addr + "\nMethod: " + method + "\n"
						+ "Response Code: " + conn.getResponseCode() + "\n"
						+ displayConnectionHeaders(conn)
						+ displayContent(conn));
			editor.setCaretPosition(0);
		}
		catch (Exception e) {
			editor.setText("Unable to establish an HttpURLConnection to " + addr + "\n\n"
				+"Exception message: " + e.getMessage());
			return;
		}
	}

	//Collect all the headers returned.
	String displayConnectionHeaders(HttpURLConnection conn) {
		int n = conn.getHeaderFields().size();
		String text = "Headers [" + n + "]" + (n>0 ? ":\n" : "\n");
		String key;
		String value;
		if (n > 0) {
			int i = 1;
			while ((key = conn.getHeaderFieldKey(i)) != null) {
				value = conn.getHeaderField(i++);
				text += key + " = " + value + "\n";
			}
		}
		return text + "\n----------------------------------------------------\n";
	}

	//Get the text returned in the connection.
	String displayContent(HttpURLConnection conn) {
		int length = conn.getContentLength();
		StringBuffer text = new StringBuffer("Content length: " + length + "\n");
		try {
			InputStream is = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			int size = 256;
			char[] buf = new char[size];
			int len;
			while ((len=isr.read(buf,0,size)) != -1) text.append(buf,0,len);
		}
		catch (Exception e) {
			text.append("Error reading the input stream\nException message: "
											+ e.getMessage() + "\n\n");
		}
		return text.toString();
	}

	//All-accepting X509 Trust Manager
	class AcceptAllX509TrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	}

	//All-verifying HostnameVerifier
	class AcceptAllHostnameVerifier implements HostnameVerifier {
		public boolean verify(String urlHost, SSLSession ssls) {
			return true;
		}
	}

	//Class to provide the buttons and text selections
	class Header extends JPanel implements ActionListener {
		public JTextField address;
		public JRadioButton getButton;
		public JRadioButton postButton;
		JButton connect;
		ButtonGroup group;

		public Header() {
			super();
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			group = new ButtonGroup();
			address = new JTextField(50);
			address.addActionListener(this);
			address.setFont(font);
			connect = new JButton("Connect");
			connect.addActionListener(this);
			getButton = new JRadioButton();
			getButton.setSelected(true);
			postButton = new JRadioButton();
			group.add(getButton);
			group.add(postButton);

			this.add(new JLabel("URL:"));
			this.add(Box.createHorizontalStrut(5));
			this.add(address);
			this.add(Box.createHorizontalStrut(10));
			this.add(connect);
			this.add(Box.createHorizontalStrut(20));

			this.add(new JLabel("GET:"));
			this.add(getButton);
			this.add(Box.createHorizontalStrut(5));
			this.add(new JLabel("POST:"));
			this.add(postButton);
		}

		public String getMethod() {
			if (postButton.isSelected()) return "POST";
			else return "GET";
		}

		public void actionPerformed(ActionEvent e) {
			String addr = address.getText().trim();
			if (addr.indexOf("://") == -1) addr = "http://" + addr;
			if (!addr.toLowerCase().startsWith("http"))
				HttpTest.message.setText("Invalid URL: the URL must start with \"http\"");
			else {
				HttpTest.message.setText(" ");
				connect(addr);
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
