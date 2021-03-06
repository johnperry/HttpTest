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
import java.util.zip.*;
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

	static final Charset utf8 = Charset.forName("UTF-8");
	static final Charset latin1 = Charset.forName("ISO-8859-1");
	static Color bg = Color.getHSBColor(0.58f, 0.17f, 0.95f);

	Header header;
	JScrollPane scroller;
	ScrollableEditorPane editor;
	Font font;
	TrustManager[] trustAllCerts;
	CookieManager cookieManager = null;
	Map<String,java.util.List<String>> headers = null;
	Map<String,java.util.List<String>> cookies = null;

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
		headers = conn.getHeaderFields();
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
			cookies = cookieManager.get(uri, conn.getHeaderFields());
			int n = cookies.size();
			text = "Cookies [" + n + "]" + (n>0 ? ":\n" : "\n");
			if (n > 0) {
				String[] keys = new String[n];
				keys = cookies.values().toArray(keys);
				for (String key : keys) {
					text += key + "\n";
				}
			}
		}
		catch (Exception ex) { }
		return text;
	}
	
	String getHeader(String name) {
		java.util.List<String> list = headers.get(name);
		if ((list != null)  && (list.size() > 0)) return list.get(0);
		return "";
	}
	
	Charset getCharset() {
		Charset charset = latin1;
		String contentType = getHeader("Content-Type");
		if (contentType.startsWith("text")) {
			int k = contentType.indexOf("charset=");
			if (k >= 0) {
				String name = contentType.substring(k + 8).trim();
				charset = Charset.forName(name);
			}
		}
		return charset;
	}		

	//Get the text returned in the connection.
	String displayContent(HttpURLConnection conn) {
		int length = conn.getContentLength();
		StringBuffer text = new StringBuffer("Content length: " + length + "\n");
		try {
			InputStream is = conn.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int size = 4096;
			byte[] buf = new byte[size];
			int len;
			while ((len=is.read(buf,0,size)) != -1) baos.write(buf,0,len);
			
			String contentType = getHeader("Content-Type");
			String contentEncoding = getHeader("Content-Encoding");
			if (!contentEncoding.equals("gzip")) {
				if (contentType.startsWith("text") || contentType.startsWith("application/json")) {
					text.append(new String(baos.toByteArray(), getCharset()));
				}
				else text.append("non-text data\n");
			}
			else {
				text.append("gzip:\n");
				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				GZIPInputStream zipped = new GZIPInputStream(bais);
				ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
				while ((len=zipped.read(buf,0,size)) != -1) unzipped.write(buf,0,len);
				if (contentType.startsWith("text")) {
					text.append(new String(unzipped.toByteArray(), getCharset()));
				}
				else text.append("non-text data\n");
			}
		}
		catch (Exception e) {
			text.append("Error processing response content.\nException message: "
											+ e.getMessage() + "\n\n");
		}
		return text.toString();
	}

	//All-accepting X509 Trust Manager
	class AcceptAllX509TrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		public void checkClientTrusted(X509Certificate[] certs, String authType) { }
		public void checkServerTrusted(X509Certificate[] certs, String authType) { }
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
			this.setLayout(new RowLayout());
			setBackground(bg);
			add(RowLayout.crlf());
			
			//Method row
			getButton = new RB("GET");
			getButton.setSelected(true);
			putButton = new RB("PUT");
			postButton = new RB("POST");
			optionsButton = new RB("OPTIONS");
			group = new ButtonGroup();
			group.add(getButton);
			group.add(putButton);
			group.add(postButton);
			group.add(optionsButton);
			connect = new JButton("Connect");
			add(connect);
			connect.addActionListener(this);
			JPanel p = new JPanel(new RowLayout());
			p.setBackground(bg);
			p.add(getButton);
			p.add(putButton);
			p.add(postButton);
			p.add(optionsButton);
			p.add(RowLayout.crlf());
			add(p);
			add(RowLayout.crlf());
			
			//URL row
			address = new JTextField(100);
			address.setText("http://"+IPUtil.getIPAddress()+":5678");
			address.addActionListener(this);
			address.setFont(font);
			add(new LBL("URL:", 1.0f));
			add(address);
			add(RowLayout.crlf());
			
			//POST body row
			body = new JTextField(100);
			body.addActionListener(this);
			body.setFont(font);
			add(new LBL("POST Body:", 1.0f));
			add(body);
			add(RowLayout.crlf());
			
			//GZIP row
			gzip = new JCheckBox("", false);
			gzip.setAlignmentX(1.0f);
			gzip.setBackground(bg);
			add(gzip);
			add(new JLabel("Include gzip Accept-Encoding header"));
			add(RowLayout.crlf());
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
	
	class RB extends JRadioButton {
		public RB(String s) {
			super(s);
			setBackground(bg);
		}
	}
	
	class LBL extends JLabel {
		public LBL(String s, float x) {
			super(s);
			setAlignmentX(x);
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
