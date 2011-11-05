/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import java.io.*;
import javax.swing.*;

/**
 * A JPanel displaying the Instructions.html resource from the jar.
 */
public class Instructions extends JPanel {

	JScrollPane scroller;
	JEditorPane editor;

	/**
	 * Class constructor; displays the program instructions.
	 */
	public Instructions() {
		super(new BorderLayout());
		scroller = new JScrollPane();
		editor = new JEditorPane();
		scroller.setViewportView(editor);
		try { editor.setPage(getClass().getResource("/Instructions.html")); }
		catch (Exception e) { editor.setText(e.getMessage()); }
		this.add(scroller,BorderLayout.CENTER);
	}

}
