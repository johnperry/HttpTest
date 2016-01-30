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
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import org.rsna.ui.RowLayout;

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

	static Charset utf8 = Charset.forName("UTF-8");

	Header header;
	JScrollPane scroller;
	ScrollableEditorPane editor;
	Font font;
	TrustManager[] trustAllCerts;
	CookieManager cookieManager = null;

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
		cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
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

			//Get the method selected (GET/PUT/POST/OPTIONS)
			String method = header.getMethod();
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
			
			//If gzip is checked, include the Accept-Encoding header.
			if (header.gzip.isSelected()) {
				conn.setRequestProperty("Accept-Encoding","gzip");
			}

			boolean sendBody = false;
			String body = "";
			if (method.equals("POST")) {
				body = header.getBody();
				if (!body.equals("")) {
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					conn.setDoOutput(true);
					sendBody = true;
				}
			}
			else if (method.equals("OPTIONS")) {
				String origin = url.getProtocol().toLowerCase()+"://"+IPUtil.getIPAddress();
				conn.setRequestProperty("Origin", origin);
				conn.setRequestProperty("Access-Control-Request-Method", "POST, GET, OPTIONS");			
			}
			
			//Turn off redirects
			conn.setFollowRedirects(false);

			//Make the connection
			conn.connect();

			//Send the body if required
			if (sendBody) {
				try {
					BufferedWriter writer =
						new BufferedWriter( new OutputStreamWriter( conn.getOutputStream(), utf8 ) );
					writer.write(body);
					writer.flush();
					writer.close();
				}
				catch (Exception ignore) { }
			}

			//And display the results.
			editor.setText(addr + "\nMethod: " + method + "\n"
						+ "Response Code: " + conn.getResponseCode() + "\n"
						+ displayConnectionHeaders(conn)
						+ displayCookies(conn, url)
						+ "\n----------------------------------------------------\n"
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
		Map<String,java.util.List<String>> headers = conn.getHeaderFields();
		int n = headers.size();
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
		return text;
	}

	//Collect all the cookies from the CookieManager.
	String displayCookies(HttpURLConnection conn, URL url) {
		String text = "Unable to get cookies\n";
		try {
			URI uri = url.toURI();
			Map<String,java.util.List<String>> cooks = cookieManager.get(uri, conn.getHeaderFields());
			int n = cooks.size();
			text = "Cookies [" + n + "]" + (n>0 ? ":\n" : "\n");
			if (n > 0) {
				String[] keys = new String[n];
				keys = cooks.values().toArray(keys);
				for (String key : keys) {
					text += key + "\n";
				}
			}
		}
		catch (Exception ex) { }
		return text;
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
		public JTextField body;
		public JCheckBox gzip;
		public JRadioButton getButton;
		public JRadioButton putButton;
		public JRadioButton postButton;
		public JRadioButton optionsButton;
		JButton connect;
		ButtonGroup group;

		public Header() {
			super();
			this.setLayout(new FlowLayout(FlowLayout.LEADING));
			group = new ButtonGroup();

			JPanel p = new JPanel(new RowLayout());
			address = new JTextField(100);
			address.setText("http://"+IPUtil.getIPAddress()+":5678");
			address.addActionListener(this);
			address.setFont(font);
			p.add(new JLabel("URL:"));
			p.add(address);
			p.add(RowLayout.crlf());
			body = new JTextField(100);
			body.addActionListener(this);
			body.setFont(font);
			p.add(new JLabel("POST Body:"));
			p.add(body);
			p.add(RowLayout.crlf());
			gzip = new JCheckBox("", false);
			p.add(gzip);
			p.add(new JLabel("Include gzip Accept-Encoding header"));
			p.add(RowLayout.crlf());

			connect = new JButton("Connect");
			connect.addActionListener(this);
			getButton = new JRadioButton();
			getButton.setSelected(true);
			putButton = new JRadioButton();
			postButton = new JRadioButton();
			optionsButton = new JRadioButton();
			group.add(getButton);
			group.add(putButton);
			group.add(postButton);
			group.add(optionsButton);

			this.add(p);
			this.add(Box.createHorizontalStrut(10));
			this.add(connect);
			this.add(Box.createHorizontalStrut(20));

			JPanel methodPanel = new JPanel(new RowLayout());
			methodPanel.add(new JLabel("GET:"));
			methodPanel.add(getButton);
			methodPanel.add(RowLayout.crlf());
			methodPanel.add(new JLabel("PUT:"));
			methodPanel.add(putButton);
			methodPanel.add(RowLayout.crlf());
			methodPanel.add(new JLabel("POST:"));
			methodPanel.add(postButton);
			methodPanel.add(RowLayout.crlf());
			methodPanel.add(new JLabel("OPTIONS:"));
			methodPanel.add(optionsButton);
			methodPanel.add(RowLayout.crlf());
			this.add(methodPanel);
		}

		public String getMethod() {
			if (putButton.isSelected()) return "PUT";
			else if (postButton.isSelected()) return "POST";
			else if (optionsButton.isSelected()) return "OPTIONS";
			else return "GET";
		}

		public String getBody() {
			if (postButton.isSelected()) return body.getText().trim();
			else return "";
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
