/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.httptest;

import java.awt.*;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.*;

/**
 * A JPanel for displaying a JLabel.
 */
public class Message extends JPanel {

	static JLabel label;

	/**
	 * Class constructor; displays a blank label and puts a nice border around the JPanel.
	 */
	public Message() {
		super();
		Border emptyBorder = BorderFactory.createEmptyBorder(3,3,3,3);
		Border bevelBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(bevelBorder,emptyBorder);
		this.setBorder(compoundBorder);
		this.setLayout(new BorderLayout());
		label = new JLabel(" ");
		this.add(label);
	}

	/**
	 * Set the text of the label.
	 * @param text the text of the label.
	 */
	public static void setText(String text) {
		label.setText(text);
	}

}

