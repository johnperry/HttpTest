/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * An Activatable JScrollPane containing a JEditorPane that displays
 * a new HTML table containing all the environment variables whenever
 * it is activated.
 */
public class EnvProps extends JScrollPane implements Activatable {

	/**
	 * Implementation of the Activatable interface; creates a new
	 * JEditorPane and loads it with the current environment variables.
	 */
	public void activate() {
		JEditorPane editor = new JEditorPane("text/html",displayEnvironment());
		editor.setEditable(false);
		editor.setBackground(Color.lightGray);
		editor.setCaretPosition(0);
		this.setViewportView(editor);
	}

	/**
	 * Implementation of the Activatable interface.
	 * Deactivation is ignored.
	 */
	public void deactivate() {
	}

	//Return a String containing the HTML rows of a table
	//displaying all the environment variables.
	private String displayEnvironment() {
		String v;
		String s =
				"<body bgcolor=white><center>\n"
			+	"<h3>Environment Variables</h3>\n"
			+	"<table border=\"1\">\n";
		String sep = System.getProperty("path.separator",";");
		StringBuffer sb = new StringBuffer(s);

		Map<String,String> env = System.getenv();
		String[] n = new String[env.size()];
		n = env.keySet().toArray(n);
		Arrays.sort(n);

		for (int i=0; i<n.length; i++) {
			v = env.get(n[i]);

			//Make path and dirs variables more readable by
			//putting each element on a separate line.
			if (n[i].toLowerCase().contains("path"))
				v = v.replace(sep, sep+"<br/>");

			sb.append( "<tr><td>" + n[i] + "</td><td>" + v + "</td></tr>\n" );
		}
		return sb.toString();
	}

}
