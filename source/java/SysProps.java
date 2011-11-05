/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * An Activatable JScrollPane containing a JEditorPane that displays
 * a new HTML table containing all the Java System properties whenever
 * it is activated.
 */
public class SysProps extends JScrollPane implements Activatable {

	/**
	 * Implementation of the Activatable interface; creates a new
	 * JEditorPane and loads it with the current Java System properties.
	 */
	public void activate() {
		JEditorPane editor = new JEditorPane("text/html",displayProperties());
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

	//Get the properties and create an HTML String to display them.
	private String displayProperties() {
		String v;
		String s =
				"<body bgcolor=white><center>\n"
			+	"<h3>System Properties</h3>\n"
			+	"<table border=\"1\">\n";
		Properties p = System.getProperties();
		String[] n = new String[p.size()];
		Enumeration e = p.propertyNames();
		for (int i=0; i< n.length; i++) n[i] = (String)e.nextElement();
		Arrays.sort(n);
		for (int i=0; i<n.length; i++) {
			v = p.getProperty(n[i]);

			//Make path properties more readable by putting
			//each element on a separate line.
			if (n[i].endsWith(".path")) v = v.replace(";",";<br>");

			s += "<tr><td>" + n[i] + "</td><td>" + v + "</td></tr>\n";
		}
		s += "</table></center></body>\n";
		return s;
	}
}
